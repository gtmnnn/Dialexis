package ru.nsu.dialexis.ui;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.nsu.dialexis.domain.ChatMessage;

public final class MessageFormatter {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private static final Pattern SERVER_STARTED =
            Pattern.compile("^Server started on (?<host>[^:]+):(?<port>\\d+)$");
    private static final Pattern CONNECTED =
            Pattern.compile("^Connected to peer (?<host>[^:]+):(?<port>\\d+)$");
    private static final Pattern REGISTERED =
            Pattern.compile("^Registered peer (?<host>[^:]+):(?<port>\\d+)$");
    private static final Pattern FAILED_CONNECT =
            Pattern.compile("^Failed to connect to peer (?<host>[^:]+):(?<port>\\d+) \\((?<details>.+)\\)$");
    private static final Pattern FAILED_SEND =
            Pattern.compile("^Failed to send message to (?<host>[^:]+):(?<port>\\d+) \\((?<details>.+)\\)$");
    private static final Pattern DISCONNECTED =
            Pattern.compile("^Disconnected from peer (?<host>[^:]+):(?<port>\\d+)$");

    /** Formats a domain chat message for user-facing output. */
    public String format(ChatMessage message) {
        Objects.requireNonNull(message);
        return String.format("[%s] %s: %s",
                FORMATTER.format(message.timestamp()),
                message.sender(),
                message.text());
    }

    /** Formats a transport or lifecycle message with the system prefix. */
    public String formatSystem(String text) {
        Objects.requireNonNull(text);
        return "[system] " + humanizeSystem(text);
    }

    /** Formats status text without adding the console system prefix. */
    public String formatStatus(String text) {
        Objects.requireNonNull(text);
        return humanizeSystem(text);
    }

    private String humanizeSystem(String text) {
        if (text.equals("Transport stopped")) {
            return "Соединение остановлено";
        }
        if (text.equals("No remote peer is connected yet. Message stays local.")) {
            return "Удалённый собеседник не подключён — сообщение осталось локально";
        }
        if (text.equals("No remote peer is connected")) {
            return "Удалённый собеседник не подключён";
        }

        Matcher started = SERVER_STARTED.matcher(text);
        if (started.matches()) {
            return "Локальный узел запущен: " + started.group("host") + ":" + started.group("port");
        }

        Matcher connected = CONNECTED.matcher(text);
        if (connected.matches()) {
            return "Подключились к собеседнику: " + connected.group("host") + ":" + connected.group("port");
        }

        Matcher registered = REGISTERED.matcher(text);
        if (registered.matches()) {
            return "Собеседник зарегистрирован: " + registered.group("host") + ":" + registered.group("port");
        }

        Matcher failedConnect = FAILED_CONNECT.matcher(text);
        if (failedConnect.matches()) {
            return "Не удалось подключиться к " + failedConnect.group("host") + ":" + failedConnect.group("port")
                    + " (" + failedConnect.group("details") + ")";
        }

        Matcher failedSend = FAILED_SEND.matcher(text);
        if (failedSend.matches()) {
            return "Не удалось отправить сообщение на " + failedSend.group("host") + ":" + failedSend.group("port")
                    + " (" + failedSend.group("details") + ")";
        }

        Matcher disconnected = DISCONNECTED.matcher(text);
        if (disconnected.matches()) {
            return "Отключились от собеседника: " + disconnected.group("host") + ":" + disconnected.group("port");
        }

        return text;
    }
}
