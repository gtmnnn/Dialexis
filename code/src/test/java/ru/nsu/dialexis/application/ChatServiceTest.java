package ru.nsu.dialexis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        ChatMessage message = chatService.sendMessage("hello");

        assertEquals("alice", message.sender());
        assertEquals("hello", message.text());
        assertNotNull(message.timestamp());
        assertEquals(message, peerSessionManager.lastMessage);
        assertEquals(message, listener.lastMessage);
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
