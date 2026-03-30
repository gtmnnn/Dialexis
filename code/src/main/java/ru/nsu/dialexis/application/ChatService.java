package ru.nsu.dialexis.application;

import java.time.Instant;
import java.util.Optional;
import java.util.Objects;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.MessageListener;

public class ChatService {
    private final String currentUserName;
    private final PeerSessionManager peerSessionManager;
    private MessageListener messageListener;

    public ChatService(String currentUserName, PeerSessionManager peerSessionManager) {
        this.currentUserName = Objects.requireNonNull(currentUserName);
        this.peerSessionManager = Objects.requireNonNull(peerSessionManager);
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public Optional<ChatMessage> sendMessage(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        ChatMessage message = new ChatMessage(currentUserName, Instant.now(), text);
        peerSessionManager.send(message);
        notifyListener(message);
        return Optional.of(message);
    }

    public void onIncomingMessage(ChatMessage message) {
        notifyListener(message);
    }

    private void notifyListener(ChatMessage message) {
        if (messageListener != null) {
            messageListener.onMessage(message);
        }
    }
}
