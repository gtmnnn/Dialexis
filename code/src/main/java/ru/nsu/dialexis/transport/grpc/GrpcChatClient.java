package ru.nsu.dialexis.transport.grpc;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;

public class GrpcChatClient {
    private final ProtoMessageMapper mapper;
    private PeerAddress remotePeer;

    public GrpcChatClient() {
        this(new ProtoMessageMapper());
    }

    public GrpcChatClient(ProtoMessageMapper mapper) {
        this.mapper = mapper;
    }

    public void connect(PeerAddress address) {
        remotePeer = address;
    }

    public void send(ChatMessage message) {
        if (remotePeer == null) {
            throw new IllegalStateException("Remote peer is not connected");
        }
        mapper.toTransport(message);
    }

    public void close() {
        remotePeer = null;
    }
}
