package ru.nsu.dialexis.transport.grpc;

import java.io.IOException;

public class GrpcChatServer {
    private boolean started;
    private int port;
    private ChatGrpcEndpoint endpoint;

    public void start(int port, ChatGrpcEndpoint endpoint) throws IOException {
        this.port = port;
        this.endpoint = endpoint;
        this.started = true;
    }

    public void stop() {
        this.started = false;
        this.endpoint = null;
    }

    public boolean isStarted() {
        return started;
    }

    public int port() {
        return port;
    }

    public ChatGrpcEndpoint endpoint() {
        return endpoint;
    }
}
