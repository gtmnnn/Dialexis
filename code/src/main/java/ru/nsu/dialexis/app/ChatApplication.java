package ru.nsu.dialexis.app;

import java.io.IOException;

import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.application.PeerSessionManager;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.transport.grpc.ChatGrpcEndpoint;
import ru.nsu.dialexis.transport.grpc.ProtoMessageMapper;
import ru.nsu.dialexis.ui.CliOptions;
import ru.nsu.dialexis.ui.ConsoleUi;

public final class ChatApplication {
    private ChatApplication() {
    }

    /** Starts the console application and wires together UI, service and transport layers. */
    public static void main(String[] args) {
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println(usage());
            return;
        }

        PeerSessionManager peerSessionManager = new PeerSessionManager();
        ChatService chatService = new ChatService(options.userName(), peerSessionManager);
        ConsoleUi consoleUi = new ConsoleUi(chatService, peerSessionManager);

        chatService.setMessageListener(consoleUi::showMessage);
        peerSessionManager.setSystemMessageListener(consoleUi::showSystemMessage);

        try {
            ChatGrpcEndpoint endpoint = new ChatGrpcEndpoint(chatService, peerSessionManager, new ProtoMessageMapper());
            peerSessionManager.startServer(options.listenPort(), endpoint);
        } catch (IOException e) {
            System.err.println("Failed to start local server: " + e.getMessage());
            return;
        }

        if (options.hasRemotePeer()) {
            peerSessionManager.connect(new PeerAddress(options.connectHost(), options.connectPort()));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(peerSessionManager::shutdown));
        consoleUi.start();
    }

    private static String usage() {
        return """
                Usage:
                  dialexis --name <username> --port <listenPort> [--connect-host <host> --connect-port <port>]

                Examples:
                  dialexis --name Alice --port 50051
                  dialexis --name Bob --port 50052 --connect-host 127.0.0.1 --connect-port 50051
                """.trim();
    }
}
