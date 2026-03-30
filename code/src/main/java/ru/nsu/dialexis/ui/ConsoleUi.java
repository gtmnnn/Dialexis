package ru.nsu.dialexis.ui;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;

import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.application.PeerSessionManager;
import ru.nsu.dialexis.domain.ChatMessage;

public class ConsoleUi {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private final ChatService chatService;
    private final PeerSessionManager peerSessionManager;

    public ConsoleUi(ChatService chatService, PeerSessionManager peerSessionManager) {
        this.chatService = Objects.requireNonNull(chatService);
        this.peerSessionManager = Objects.requireNonNull(peerSessionManager);
    }

    public void start() {
        showSystemMessage("Chat started. Type messages below. Press Ctrl+C to exit.");
        readLoop();
    }

    public void readLoop() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    showSystemMessage("Empty input ignored");
                    continue;
                }
                chatService.sendMessage(trimmed);
            }
        } finally {
            peerSessionManager.shutdown();
        }
    }

    public void showMessage(ChatMessage message) {
        System.out.println(formatMessage(message));
    }

    public String formatMessage(ChatMessage message) {
        return String.format("[%s] %s: %s",
                FORMATTER.format(message.timestamp()),
                message.sender(),
                message.text());
    }

    public void showSystemMessage(String text) {
        System.out.println("[system] " + text);
    }
}
