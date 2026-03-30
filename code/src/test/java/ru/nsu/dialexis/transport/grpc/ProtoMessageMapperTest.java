package ru.nsu.dialexis.transport.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ru.nsu.dialexis.domain.ChatMessage;

class ProtoMessageMapperTest {
    private final ProtoMessageMapper mapper = new ProtoMessageMapper();

    @Test
    void mapsDomainMessageToProto() {
        ChatMessage message = new ChatMessage("alice", Instant.parse("2026-03-30T10:15:30Z"), "hello");

        TransportChatMessage request = mapper.toTransport(message);

        assertEquals("alice", request.sender());
        assertEquals("2026-03-30T10:15:30Z", request.timestamp());
        assertEquals("hello", request.text());
    }

    @Test
    void mapsProtoToDomainMessage() {
        TransportChatMessage request = new TransportChatMessage("bob", "2026-03-30T10:15:30Z", "hi");

        ChatMessage message = mapper.fromTransport(request);

        assertEquals("bob", message.sender());
        assertEquals(Instant.parse("2026-03-30T10:15:30Z"), message.timestamp());
        assertEquals("hi", message.text());
    }
}
