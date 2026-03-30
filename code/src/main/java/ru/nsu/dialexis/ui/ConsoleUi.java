package ru.nsu.dialexis.ui;

import java.util.Objects;
import java.util.Scanner;

import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.application.PeerSessionManager;
import ru.nsu.dialexis.domain.ChatMessage;

public class ConsoleUi {
    private final ChatService chatService;
    private final PeerSessionManager peerSessionManager;
    private final MessageFormatter formatter = new MessageFormatter();

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
        System.out.println(formatter.format(message));
    }

    public void showSystemMessage(String text) {
        System.out.println(formatter.formatSystem(text));
    }
}
