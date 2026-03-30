package ru.nsu.dialexis.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ru.nsu.dialexis.domain.ChatMessage;

class ConsoleUiFormatTest {
    @Test
    void formatsMessageAsTimestampSenderText() {
        MessageFormatter formatter = new MessageFormatter();
        ChatMessage message = new ChatMessage("bob", Instant.parse("2026-03-30T10:15:30Z"), "hello");

        String formatted = formatter.format(message);

        assertEquals("[2026-03-30 10:15:30] bob: hello", formatted);
    }
}

