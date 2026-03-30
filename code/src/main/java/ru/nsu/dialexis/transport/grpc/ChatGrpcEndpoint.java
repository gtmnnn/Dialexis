package ru.nsu.dialexis.transport.grpc;

import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.domain.ChatMessage;

public class ChatGrpcEndpoint {
    private final ChatService chatService;
    private final ProtoMessageMapper mapper;

    public ChatGrpcEndpoint(ChatService chatService, ProtoMessageMapper mapper) {
        this.chatService = chatService;
        this.mapper = mapper;
    }

    public void accept(TransportChatMessage request) {
        ChatMessage message = mapper.fromTransport(request);
        chatService.onIncomingMessage(message);
    }
}
