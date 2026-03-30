package ru.nsu.dialexis.transport.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.proto.ChatMessageRequest;

class ProtoMessageMapperTest {
    private final ProtoMessageMapper mapper = new ProtoMessageMapper();

    @Test
    void mapsDomainMessageToProto() {
        ChatMessage message = new ChatMessage(
                "alice",
                Instant.parse("2026-03-30T10:15:30Z"),
                "hello",
                new PeerAddress("127.0.0.1", 5005));

        ChatMessageRequest request = mapper.toProto(message);

        assertEquals("alice", request.getSender());
        assertEquals("2026-03-30T10:15:30Z", request.getTimestamp());
        assertEquals("hello", request.getText());
        assertEquals("127.0.0.1", request.getSenderHost());
        assertEquals(5005, request.getSenderPort());
    }

    @Test
    void mapsProtoToDomainMessage() {
        ChatMessageRequest request = ChatMessageRequest.newBuilder()
                .setSender("bob")
                .setTimestamp("2026-03-30T10:15:30Z")
                .setText("hi")
                .setSenderHost("127.0.0.1")
                .setSenderPort(5006)
                .build();

        ChatMessage message = mapper.fromProto(request);

        assertEquals("bob", message.sender());
        assertEquals(Instant.parse("2026-03-30T10:15:30Z"), message.timestamp());
        assertEquals("hi", message.text());
        assertEquals(new PeerAddress("127.0.0.1", 5006), message.replyTo());
    }
}
