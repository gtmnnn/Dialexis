package ru.nsu.dialexis.domain;

import java.time.Instant;
import java.util.Objects;

public record ChatMessage(String sender, Instant timestamp, String text) {
    public ChatMessage {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(text);
    }
}
