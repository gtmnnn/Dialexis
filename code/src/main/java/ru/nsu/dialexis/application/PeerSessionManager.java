package ru.nsu.dialexis.application;

import java.io.IOException;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.transport.grpc.ChatGrpcEndpoint;
import ru.nsu.dialexis.transport.grpc.GrpcChatClient;
import ru.nsu.dialexis.transport.grpc.GrpcChatServer;

public class PeerSessionManager {
    private final GrpcChatServer server;
    private final GrpcChatClient client;
    private PeerAddress remotePeer;

    public PeerSessionManager() {
        this(new GrpcChatServer(), new GrpcChatClient());
    }

    public PeerSessionManager(GrpcChatServer server, GrpcChatClient client) {
        this.server = server;
        this.client = client;
    }

    public void startServer(int port, ChatGrpcEndpoint endpoint) throws IOException {
        server.start(port, endpoint);
    }

    public void connect(PeerAddress address) {
        client.connect(address);
        remotePeer = address;
    }

    public void send(ChatMessage message) {
        if (remotePeer == null) {
            return;
        }
        client.send(message);
    }

    public void shutdown() {
        client.close();
        server.stop();
        remotePeer = null;
    }
}
