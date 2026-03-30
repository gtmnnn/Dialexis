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

    public static void main(String[] args) throws IOException, InterruptedException {
        CliOptions options = CliOptions.parse(args);

        PeerSessionManager peerSessionManager = new PeerSessionManager();
        ChatService chatService = new ChatService(options.userName(), peerSessionManager);
        ConsoleUi consoleUi = new ConsoleUi(chatService);

        chatService.setMessageListener(consoleUi::showMessage);

        ChatGrpcEndpoint endpoint = new ChatGrpcEndpoint(chatService, new ProtoMessageMapper());
        peerSessionManager.startServer(options.listenPort(), endpoint);
        consoleUi.showSystemMessage("Local node is listening on port " + options.listenPort());

        if (options.hasRemotePeer()) {
            peerSessionManager.connect(new PeerAddress(options.connectHost(), options.connectPort()));
            consoleUi.showSystemMessage("Remote peer configured: "
                    + options.connectHost() + ":" + options.connectPort());
        } else {
            consoleUi.showSystemMessage("No remote peer configured. Running in server mode.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(peerSessionManager::shutdown));
        consoleUi.start();
    }
}
