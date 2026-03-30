package ru.nsu.dialexis.transport.grpc;

import io.grpc.stub.StreamObserver;
import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.proto.ChatEndpointGrpc;
import ru.nsu.dialexis.proto.ChatMessageAck;
import ru.nsu.dialexis.proto.ChatMessageRequest;

public class ChatGrpcEndpoint extends ChatEndpointGrpc.ChatEndpointImplBase {
    private final ChatService chatService;
    private final ProtoMessageMapper mapper;

    public ChatGrpcEndpoint(ChatService chatService, ProtoMessageMapper mapper) {
        this.chatService = chatService;
        this.mapper = mapper;
    }

    @Override
    public void sendMessage(ChatMessageRequest request, StreamObserver<ChatMessageAck> responseObserver) {
        ChatMessage message = mapper.fromProto(request);
        chatService.onIncomingMessage(message);

        responseObserver.onNext(ChatMessageAck.newBuilder()
                .setDelivered(true)
                .build());
        responseObserver.onCompleted();
    }
}
