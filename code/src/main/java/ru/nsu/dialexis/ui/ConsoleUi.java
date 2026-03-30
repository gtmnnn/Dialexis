package ru.nsu.dialexis.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.domain.ChatMessage;

public class ConsoleUi {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ChatService chatService;

    public ConsoleUi(ChatService chatService) {
        this.chatService = chatService;
    }

    public void start() {
        showSystemMessage("Chat started. Type messages below.");
        readLoop();
    }

    public void readLoop() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line == null || line.isBlank()) {
                continue;
            }
            chatService.sendMessage(line);
        }
    }

    public void showMessage(ChatMessage message) {
        System.out.printf("[%s] %s: %s%n",
                FORMATTER.format(message.timestamp()),
                message.sender(),
                message.text());
    }

    public void showSystemMessage(String text) {
        System.out.println("[system] " + text);
    }
}
