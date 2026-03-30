package ru.nsu.dialexis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.MessageListener;
import ru.nsu.dialexis.domain.PeerAddress;

class ChatServiceTest {
    @Test
    void sendMessageCreatesDomainMessageAndNotifiesListener() {
        RecordingPeerSessionManager peerSessionManager = new RecordingPeerSessionManager();
        RecordingMessageListener listener = new RecordingMessageListener();
        ChatService chatService = new ChatService("alice", peerSessionManager);
        chatService.setMessageListener(listener);

        ChatMessage message = chatService.sendMessage("hello");

        assertEquals("alice", message.sender());
        assertEquals("hello", message.text());
        assertNotNull(message.timestamp());
        assertEquals(new PeerAddress("127.0.0.1", 5000), message.replyTo());
        assertEquals(message, peerSessionManager.lastMessage);
        assertEquals(message, listener.lastMessage);
    }

    @Test
    void incomingMessageRegistersPeerForReply() {
        RecordingPeerSessionManager peerSessionManager = new RecordingPeerSessionManager();
        RecordingMessageListener listener = new RecordingMessageListener();
        ChatService chatService = new ChatService("alice", peerSessionManager);
        chatService.setMessageListener(listener);

        ChatMessage incoming = new ChatMessage(
                "bob",
                java.time.Instant.parse("2026-03-30T10:15:30Z"),
                "hello",
                new PeerAddress("127.0.0.1", 5006));

        chatService.onIncomingMessage(incoming);

        assertEquals(new PeerAddress("127.0.0.1", 5006), peerSessionManager.registeredPeer);
        assertEquals(incoming, listener.lastMessage);
    }

    private static final class RecordingPeerSessionManager extends PeerSessionManager {
        private ChatMessage lastMessage;
        private PeerAddress registeredPeer;

        @Override
        public PeerAddress localAddress() {
            return new PeerAddress("127.0.0.1", 5000);
        }

        @Override
        public void send(ChatMessage message) {
            this.lastMessage = message;
        }

        @Override
        public void registerRemotePeer(PeerAddress address) {
            this.registeredPeer = address;
        }
    }

    private static final class RecordingMessageListener implements MessageListener {
        private ChatMessage lastMessage;

        @Override
        public void onMessage(ChatMessage message) {
            this.lastMessage = message;
        }
    }
}
