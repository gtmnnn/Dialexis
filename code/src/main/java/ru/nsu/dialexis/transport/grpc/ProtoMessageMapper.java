package ru.nsu.dialexis.transport.grpc;

import java.time.Instant;

import ru.nsu.dialexis.domain.ChatMessage;

public class ProtoMessageMapper {
    public TransportChatMessage toTransport(ChatMessage message) {
        return new TransportChatMessage(
                message.sender(),
                message.timestamp().toString(),
                message.text());
    }

    public ChatMessage fromTransport(TransportChatMessage message) {
        return new ChatMessage(
                message.sender(),
                Instant.parse(message.timestamp()),
                message.text());
    }
}
