package ru.nsu.dialexis.transport.grpc;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcChatServer {
    private Server server;

    /** Starts the local gRPC server on the requested port. */
    public void start(int port, ChatGrpcEndpoint endpoint) throws IOException {
        stop();
        server = ServerBuilder.forPort(port)
                .addService(endpoint)
                .build()
                .start();
    }

    /** Stops the local gRPC server if it is running. */
    public void stop() {
        if (server != null) {
            server.shutdownNow();
            server = null;
        }
    }

    /** Returns whether the underlying gRPC server is currently active. */
    public boolean isStarted() {
        return server != null && !server.isShutdown();
    }

    /** Returns the actual server port after startup. */
    public int port() {
        if (server == null) {
            throw new IllegalStateException("Server is not started");
        }
        return server.getPort();
    }
}
