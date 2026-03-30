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

    /** Creates a gRPC client with the default protobuf mapper. */
    public GrpcChatClient() {
        this(new ProtoMessageMapper());
    }

    /** Creates a gRPC client with a custom mapper, mainly for tests. */
    public GrpcChatClient(ProtoMessageMapper mapper) {
        this.mapper = mapper;
    }

    /** Opens a plaintext gRPC channel to the remote peer. */
    public void connect(PeerAddress address) {
        close();
        channel = ManagedChannelBuilder.forAddress(address.host(), address.port())
                .usePlaintext()
                .build();
        blockingStub = ChatEndpointGrpc.newBlockingStub(channel);
    }

    /** Sends one chat message over the active gRPC channel. */
    public void send(ChatMessage message) {
        if (blockingStub == null) {
            throw new IllegalStateException("Remote peer is not connected");
        }
        blockingStub.sendMessage(mapper.toProto(message));
    }

    /** Performs peer registration handshake against the active remote endpoint. */
    public void registerPeer(PeerAddress address) {
        if (blockingStub == null) {
            throw new IllegalStateException("Remote peer is not connected");
        }
        blockingStub.registerPeer(mapper.toRegistrationProto(address));
    }

    /** Closes the current gRPC channel if it exists. */
    public void close() {
        blockingStub = null;
        if (channel != null) {
            channel.shutdownNow();
            channel = null;
        }
    }
}
