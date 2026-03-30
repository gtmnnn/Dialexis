package ru.nsu.dialexis.application;

import java.io.IOException;
import java.util.function.Consumer;

import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.transport.grpc.ChatGrpcEndpoint;
import ru.nsu.dialexis.transport.grpc.GrpcChatClient;
import ru.nsu.dialexis.transport.grpc.GrpcChatServer;

public class PeerSessionManager {
    private final GrpcChatServer server;
    private final GrpcChatClient client;
    private Consumer<String> systemMessageListener;
    private PeerAddress localAddress;
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
        localAddress = new PeerAddress("127.0.0.1", server.port());
        emitSystemMessage("Server started on " + localAddress.host() + ":" + localAddress.port());
    }

    public void connect(PeerAddress address) {
        if (localAddress == null) {
            throw new IllegalStateException("Local server must be started before connecting to a peer");
        }
        client.connect(address);
        remotePeer = address;
        client.registerPeer(localAddress);
        emitSystemMessage("Connected to peer " + address.host() + ":" + address.port());
    }

    public void send(ChatMessage message) {
        if (remotePeer == null) {
            emitSystemMessage("No remote peer is connected yet. Message stays local.");
            return;
        }
        client.send(message);
        emitSystemMessage("Sent message to " + remotePeer.host() + ":" + remotePeer.port());
    }

    public void setSystemMessageListener(Consumer<String> systemMessageListener) {
        this.systemMessageListener = systemMessageListener;
    }

    public void registerRemotePeer(PeerAddress address) {
        if (address == null) {
            return;
        }
        if (remotePeer == null || !remotePeer.equals(address)) {
            client.connect(address);
            remotePeer = address;
            emitSystemMessage("Registered peer " + address.host() + ":" + address.port());
        }
    }

    public void shutdown() {
        client.close();
        server.stop();
        emitSystemMessage("Transport stopped");
        localAddress = null;
        remotePeer = null;
    }

    private void emitSystemMessage(String text) {
        if (systemMessageListener != null) {
            systemMessageListener.accept(text);
        }
    }
}
