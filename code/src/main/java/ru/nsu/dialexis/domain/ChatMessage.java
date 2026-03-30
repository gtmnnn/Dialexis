package ru.nsu.dialexis.domain;

import java.time.Instant;
import java.util.Objects;

public record ChatMessage(String sender, Instant timestamp, String text, PeerAddress replyTo) {
    public ChatMessage {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(text);
    }

    public ChatMessage(String sender, Instant timestamp, String text) {
        this(sender, timestamp, text, null);
    }
}
