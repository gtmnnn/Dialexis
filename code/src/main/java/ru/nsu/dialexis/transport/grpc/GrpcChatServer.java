package ru.nsu.dialexis.transport.grpc;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcChatServer {
    private Server server;

    public void start(int port, ChatGrpcEndpoint endpoint) throws IOException {
        stop();
        server = ServerBuilder.forPort(port)
                .addService(endpoint)
                .build()
                .start();
    }

    public void stop() {
        if (server != null) {
            server.shutdownNow();
            server = null;
        }
    }

    public boolean isStarted() {
        return server != null && !server.isShutdown();
    }

    public int port() {
        if (server == null) {
            throw new IllegalStateException("Server is not started");
        }
        return server.getPort();
    }
}
