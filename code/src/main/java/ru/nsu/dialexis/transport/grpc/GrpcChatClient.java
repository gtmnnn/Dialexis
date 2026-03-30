package ru.nsu.dialexis.transport.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.proto.ChatEndpointGrpc;

public class GrpcChatClient {
    private final ProtoMessageMapper mapper;
    private ManagedChannel channel;
    private ChatEndpointGrpc.ChatEndpointBlockingStub blockingStub;

    public GrpcChatClient() {
        this(new ProtoMessageMapper());
    }

    public GrpcChatClient(ProtoMessageMapper mapper) {
        this.mapper = mapper;
    }

    public void connect(PeerAddress address) {
        close();
        channel = ManagedChannelBuilder.forAddress(address.host(), address.port())
                .usePlaintext()
                .build();
        blockingStub = ChatEndpointGrpc.newBlockingStub(channel);
    }

    public void send(ChatMessage message) {
        if (blockingStub == null) {
            throw new IllegalStateException("Remote peer is not connected");
        }
        blockingStub.sendMessage(mapper.toProto(message));
    }

    public void close() {
        blockingStub = null;
        if (channel != null) {
            channel.shutdownNow();
            channel = null;
        }
    }
}
