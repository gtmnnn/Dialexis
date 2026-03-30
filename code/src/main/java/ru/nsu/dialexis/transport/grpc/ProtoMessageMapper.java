package ru.nsu.dialexis.transport.grpc;

import java.time.Instant;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.proto.ChatMessageRequest;

public class ProtoMessageMapper {
    public ChatMessageRequest toProto(ChatMessage message) {
        ChatMessageRequest.Builder builder = ChatMessageRequest.newBuilder()
                .setSender(message.sender())
                .setTimestamp(message.timestamp().toString())
                .setText(message.text());

        if (message.replyTo() != null) {
            builder.setSenderHost(message.replyTo().host());
            builder.setSenderPort(message.replyTo().port());
        }

        return builder.build();
    }

    public ChatMessage fromProto(ChatMessageRequest message) {
        PeerAddress replyTo = null;
        if (!message.getSenderHost().isBlank() && message.getSenderPort() > 0) {
            replyTo = new PeerAddress(message.getSenderHost(), message.getSenderPort());
        }

        return new ChatMessage(
                message.getSender(),
                Instant.parse(message.getTimestamp()),
                message.getText(),
                replyTo);
    }
}
