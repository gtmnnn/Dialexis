package ru.nsu.dialexis.application;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import io.grpc.StatusRuntimeException;
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
    private volatile boolean shutdownRequested;

    public PeerSessionManager() {
        this(new GrpcChatServer(), new GrpcChatClient());
    }

    public PeerSessionManager(GrpcChatServer server, GrpcChatClient client) {
        this.server = Objects.requireNonNull(server);
        this.client = Objects.requireNonNull(client);
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
        try {
            client.connect(address);
            remotePeer = address;
            client.registerPeer(localAddress);
            emitSystemMessage("Connected to peer " + address.host() + ":" + address.port());
        } catch (StatusRuntimeException e) {
            String details = e.getStatus().getDescription();
            emitSystemMessage("Failed to connect to peer " + address.host() + ":" + address.port()
                    + " (" + e.getStatus().getCode()
                    + (details == null || details.isBlank() ? "" : ": " + details) + ")");
            remotePeer = null;
        } catch (RuntimeException e) {
            emitSystemMessage("Failed to connect to peer " + address.host() + ":" + address.port()
                    + " (" + e.getClass().getSimpleName() + ")");
            remotePeer = null;
        }
    }

    public void send(ChatMessage message) {
        if (remotePeer == null) {
            emitSystemMessage("No remote peer is connected yet. Message stays local.");
            return;
        }
        try {
            client.send(message);
        } catch (StatusRuntimeException e) {
            String details = e.getStatus().getDescription();
            emitSystemMessage("Failed to send message to " + remotePeer.host() + ":" + remotePeer.port()
                    + " (" + e.getStatus().getCode()
                    + (details == null || details.isBlank() ? "" : ": " + details) + ")");
        } catch (RuntimeException e) {
            emitSystemMessage("Failed to send message to " + remotePeer.host() + ":" + remotePeer.port()
                    + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    public void setSystemMessageListener(Consumer<String> systemMessageListener) {
        this.systemMessageListener = systemMessageListener;
    }

    public void registerRemotePeer(PeerAddress address) {
        if (address == null) {
            return;
        }
        if (remotePeer == null || !remotePeer.equals(address)) {
            try {
                client.connect(address);
                remotePeer = address;
                emitSystemMessage("Registered peer " + address.host() + ":" + address.port());
            } catch (RuntimeException e) {
                emitSystemMessage("Failed to register peer " + address.host() + ":" + address.port()
                        + " (" + e.getClass().getSimpleName() + ")");
            }
        }
    }

    public void disconnect() {
        if (remotePeer == null) {
            emitSystemMessage("No remote peer is connected");
            return;
        }
        client.close();
        PeerAddress previous = remotePeer;
        remotePeer = null;
        emitSystemMessage("Disconnected from peer " + previous.host() + ":" + previous.port());
    }

    public void shutdown() {
        if (shutdownRequested) {
            return;
        }
        shutdownRequested = true;
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
