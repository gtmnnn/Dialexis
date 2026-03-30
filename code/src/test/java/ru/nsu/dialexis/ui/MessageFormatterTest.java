package ru.nsu.dialexis.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ru.nsu.dialexis.domain.ChatMessage;

class MessageFormatterTest {
    @Test
    void formatsChatMessageInExpectedShape() {
        MessageFormatter formatter = new MessageFormatter();
        ChatMessage message = new ChatMessage("alice", Instant.parse("2026-03-30T10:15:30Z"), "hello");

        assertEquals("[2026-03-30 10:15:30] alice: hello", formatter.format(message));
    }

    @Test
    void formatsSystemMessageWithPrefix() {
        MessageFormatter formatter = new MessageFormatter();

        assertEquals("[system] Started", formatter.formatSystem("Started"));
    }

    @Test
    void formatsStatusWithoutPrefix() {
        MessageFormatter formatter = new MessageFormatter();

        assertEquals("Started", formatter.formatStatus("Started"));
    }
}

