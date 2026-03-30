package ru.nsu.dialexis.transport.grpc;

public record TransportChatMessage(String sender, String timestamp, String text) {
}
