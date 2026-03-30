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

    /** Creates the chat service for a concrete local user and transport session. */
    public ChatService(String currentUserName, PeerSessionManager peerSessionManager) {
        this.currentUserName = Objects.requireNonNull(currentUserName);
        this.peerSessionManager = Objects.requireNonNull(peerSessionManager);
    }

    /** Registers a listener that receives locally sent and remotely received messages. */
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    /** Builds and sends a message if the text is not blank. */
    public Optional<ChatMessage> sendMessage(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        ChatMessage message = new ChatMessage(currentUserName, Instant.now(), text);
        peerSessionManager.send(message);
        notifyListener(message);
        return Optional.of(message);
    }

    /** Pushes an incoming message to the UI-facing listener. */
    public void onIncomingMessage(ChatMessage message) {
        notifyListener(message);
    }

    private void notifyListener(ChatMessage message) {
        if (messageListener != null) {
            messageListener.onMessage(message);
        }
    }
}
