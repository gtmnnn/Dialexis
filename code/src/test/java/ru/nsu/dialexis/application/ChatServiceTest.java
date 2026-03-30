package ru.nsu.dialexis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.MessageListener;

class ChatServiceTest {
    @Test
    void sendMessageCreatesDomainMessageAndNotifiesListener() {
        RecordingPeerSessionManager peerSessionManager = new RecordingPeerSessionManager();
        RecordingMessageListener listener = new RecordingMessageListener();
        ChatService chatService = new ChatService("alice", peerSessionManager);
        chatService.setMessageListener(listener);

        var result = chatService.sendMessage("hello");

        assertTrue(result.isPresent());
        ChatMessage message = result.get();

        assertEquals("alice", message.sender());
        assertEquals("hello", message.text());
        assertNotNull(message.timestamp());
        assertEquals(message, peerSessionManager.lastMessage);
        assertEquals(message, listener.lastMessage);
    }

    @Test
    void sendMessageIgnoresBlankText() {
        RecordingPeerSessionManager peerSessionManager = new RecordingPeerSessionManager();
        RecordingMessageListener listener = new RecordingMessageListener();
        ChatService chatService = new ChatService("alice", peerSessionManager);
        chatService.setMessageListener(listener);

        var result = chatService.sendMessage("   ");

        assertFalse(result.isPresent());
        assertNull(peerSessionManager.lastMessage);
        assertNull(listener.lastMessage);
    }

    @Test
    void incomingMessageNotifiesListener() {
        RecordingPeerSessionManager peerSessionManager = new RecordingPeerSessionManager();
        RecordingMessageListener listener = new RecordingMessageListener();
        ChatService chatService = new ChatService("alice", peerSessionManager);
        chatService.setMessageListener(listener);
        ChatMessage incoming = new ChatMessage("bob", java.time.Instant.parse("2026-03-30T10:15:30Z"), "hi");

        chatService.onIncomingMessage(incoming);

        assertEquals(incoming, listener.lastMessage);
    }

    private static final class RecordingPeerSessionManager extends PeerSessionManager {
        private ChatMessage lastMessage;

        @Override
        public void send(ChatMessage message) {
            this.lastMessage = message;
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
