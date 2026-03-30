package ru.nsu.dialexis.transport.grpc;

import java.time.Instant;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.proto.ChatMessageRequest;
import ru.nsu.dialexis.proto.PeerRegistrationRequest;

public class ProtoMessageMapper {
    public ChatMessageRequest toProto(ChatMessage message) {
        return ChatMessageRequest.newBuilder()
                .setSender(message.sender())
                .setTimestamp(message.timestamp().toString())
                .setText(message.text())
                .build();
    }

    public ChatMessage fromProto(ChatMessageRequest message) {
        return new ChatMessage(
                message.getSender(),
                Instant.parse(message.getTimestamp()),
                message.getText());
    }

    public PeerRegistrationRequest toRegistrationProto(PeerAddress address) {
        return PeerRegistrationRequest.newBuilder()
                .setHost(address.host())
                .setPort(address.port())
                .build();
    }

    public PeerAddress fromRegistrationProto(PeerRegistrationRequest request) {
        return new PeerAddress(request.getHost(), request.getPort());
    }
}
