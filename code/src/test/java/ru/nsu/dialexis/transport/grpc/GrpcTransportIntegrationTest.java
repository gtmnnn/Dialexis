package ru.nsu.dialexis.transport.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.application.PeerSessionManager;
import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;

class GrpcTransportIntegrationTest {
    @Test
    void deliversMessageBetweenPeers() throws Exception {
        int port = findFreePort();
        PeerSessionManager receiverSession = new PeerSessionManager();
        ChatService receiverService = new ChatService("bob", receiverSession);
        RecordingListener listener = new RecordingListener();
        receiverService.setMessageListener(listener);

        ChatGrpcEndpoint endpoint = new ChatGrpcEndpoint(receiverService, new ProtoMessageMapper());
        receiverSession.startServer(port, endpoint);

        try {
            GrpcChatClient client = new GrpcChatClient(new ProtoMessageMapper());
            client.connect(new PeerAddress("127.0.0.1", port));
            client.send(new ChatMessage("alice", java.time.Instant.parse("2026-03-30T10:15:30Z"), "hello"));

            listener.awaitMessage();
            assertNotNull(listener.lastMessage);
            assertEquals("alice", listener.lastMessage.sender());
            assertEquals("hello", listener.lastMessage.text());
        } finally {
            receiverSession.shutdown();
        }
    }

    @Test
    void failsWhenPeerIsUnavailable() {
        GrpcChatClient client = new GrpcChatClient(new ProtoMessageMapper());
        client.connect(new PeerAddress("127.0.0.1", findUnusedPort()));

        try {
            assertThrows(StatusRuntimeException.class,
                    () -> client.send(new ChatMessage("alice", java.time.Instant.now(), "hello")));
        } finally {
            client.close();
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    private static int findUnusedPort() {
        try {
            return findFreePort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to allocate test port", e);
        }
    }

    private static final class RecordingListener implements ru.nsu.dialexis.domain.MessageListener {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile ChatMessage lastMessage;

        @Override
        public void onMessage(ChatMessage message) {
            this.lastMessage = message;
            latch.countDown();
        }

        void awaitMessage() throws InterruptedException {
            if (!latch.await(Duration.ofSeconds(3).toMillis(), TimeUnit.MILLISECONDS)) {
                throw new AssertionError("Message was not delivered in time");
            }
        }
    }
}
