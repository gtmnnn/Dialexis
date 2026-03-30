package ru.nsu.dialexis.app;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.nsu.dialexis.application.ChatService;
import ru.nsu.dialexis.application.PeerSessionManager;
import ru.nsu.dialexis.domain.ChatMessage;
import ru.nsu.dialexis.domain.PeerAddress;
import ru.nsu.dialexis.transport.grpc.ChatGrpcEndpoint;
import ru.nsu.dialexis.transport.grpc.ProtoMessageMapper;
import ru.nsu.dialexis.ui.MessageFormatter;

public class GuiApplication extends Application {
    private final MessageFormatter formatter = new MessageFormatter();

    private TextField nameField;
    private TextField listenPortField;
    private TextField peerHostField;
    private TextField peerPortField;
    private Button startButton;
    private Button connectButton;
    private Button disconnectButton;
    private Button exitButton;

    private ListView<UiLine> historyList;
    private TextField inputField;
    private Button sendButton;
    private Label statusLabel;

    private volatile PeerSessionManager sessionManager;
    private volatile ChatService chatService;
    private volatile boolean started;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dialexis");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));

        root.setTop(buildConnectionPane());
        root.setCenter(buildHistoryPane());
        root.setBottom(buildInputPane());

        setUiState(false);

        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui.css")).toExternalForm());
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        appendSystem("Готово");
    }

    private VBox buildConnectionPane() {
        nameField = new TextField();
        listenPortField = new TextField();
        peerHostField = new TextField();
        peerPortField = new TextField();

        nameField.setPromptText("Имя");
        listenPortField.setPromptText("50051");
        peerHostField.setPromptText("127.0.0.1");
        peerPortField.setPromptText("50052");

        startButton = new Button("Start");
        connectButton = new Button("Connect");
        disconnectButton = new Button("Disconnect");
        exitButton = new Button("Exit");
        connectButton.getStyleClass().add("secondary");
        disconnectButton.getStyleClass().add("secondary");
        exitButton.getStyleClass().addAll("danger");

        startButton.setOnAction(e -> onStartStop());
        connectButton.setOnAction(e -> onConnect());
        disconnectButton.setOnAction(e -> onDisconnect());
        exitButton.setOnAction(e -> {
            shutdown();
            Platform.exit();
        });

        Label title = new Label("Dialexis");
        title.getStyleClass().add("title");
        Label subtitle = new Label("P2P чат поверх gRPC");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(2, title, subtitle);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        grid.add(new Label("Имя"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Локальный порт"), 2, 0);
        grid.add(listenPortField, 3, 0);

        grid.add(new Label("Адрес собеседника"), 0, 1);
        grid.add(peerHostField, 1, 1);
        grid.add(new Label("Порт собеседника"), 2, 1);
        grid.add(peerPortField, 3, 1);

        HBox buttons = new HBox(10, startButton, connectButton, disconnectButton, exitButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, header, grid, buttons);
        card.getStyleClass().add("card");

        VBox wrapper = new VBox(12, card);
        wrapper.setPadding(new Insets(0, 0, 12, 0));
        return wrapper;
    }

    private VBox buildHistoryPane() {
        historyList = new ListView<>();
        historyList.setCellFactory(new UiLineCellFactory());

        statusLabel = new Label();
        statusLabel.getStyleClass().addAll("subtitle", "status");

        VBox card = new VBox(10, historyList, statusLabel);
        card.getStyleClass().add("card");
        VBox.setVgrow(historyList, Priority.ALWAYS);

        VBox box = new VBox(card);
        VBox.setVgrow(card, Priority.ALWAYS);
        return box;
    }

    private VBox buildInputPane() {
        inputField = new TextField();
        inputField.setPromptText("Сообщение...");
        sendButton = new Button("Send");
        sendButton.getStyleClass().add("secondary");

        sendButton.setOnAction(e -> onSend());
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onSend();
            }
        });

        HBox row = new HBox(10, inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        VBox wrapper = new VBox(10, row);
        wrapper.setPadding(new Insets(10, 0, 0, 0));
        return wrapper;
    }

    private void onStartStop() {
        if (started) {
            shutdown();
            setUiState(false);
            appendSystem("Соединение остановлено");
            return;
        }

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            appendSystem("Введите имя");
            return;
        }

        Integer port = parsePort(listenPortField.getText());
        if (port == null) {
            appendSystem("Неверный локальный порт");
            return;
        }

        PeerSessionManager manager = new PeerSessionManager();
        ChatService service = new ChatService(name, manager);
        manager.setSystemMessageListener(this::appendSystem);
        service.setMessageListener(m -> Platform.runLater(() -> appendChat(m)));

        try {
            ChatGrpcEndpoint endpoint = new ChatGrpcEndpoint(service, manager, new ProtoMessageMapper());
            manager.startServer(port, endpoint);
        } catch (IOException e) {
            appendSystem("Не удалось запустить локальный узел: " + e.getMessage());
            return;
        }

        sessionManager = manager;
        chatService = service;
        started = true;
        setUiState(true);
        appendSystem("Запущено");
    }

    private void onConnect() {
        if (!started || sessionManager == null) {
            appendSystem("Сначала нажмите Start");
            return;
        }

        String host = peerHostField.getText() == null ? "" : peerHostField.getText().trim();
        if (host.isEmpty()) {
            appendSystem("Введите адрес собеседника");
            return;
        }

        Integer port = parsePort(peerPortField.getText());
        if (port == null) {
            appendSystem("Неверный порт собеседника");
            return;
        }

        sessionManager.connect(new PeerAddress(host, port));
    }

    private void onDisconnect() {
        if (!started || sessionManager == null) {
            appendSystem("Сначала нажмите Start");
            return;
        }
        sessionManager.disconnect();
    }

    private void onSend() {
        if (!started || chatService == null) {
            appendSystem("Сначала нажмите Start");
            return;
        }

        String text = inputField.getText();
        if (text == null) {
            return;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            appendSystem("Пустое сообщение не отправлено");
            inputField.clear();
            return;
        }

        Optional<ChatMessage> sent = chatService.sendMessage(trimmed);
        if (sent.isPresent()) {
            inputField.clear();
        }
    }

    private Integer parsePort(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            int port = Integer.parseInt(trimmed);
            if (port < 1 || port > 65535) {
                return null;
            }
            return port;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setUiState(boolean chatEnabled) {
        connectButton.setDisable(!chatEnabled);
        disconnectButton.setDisable(!chatEnabled);
        inputField.setDisable(!chatEnabled);
        sendButton.setDisable(!chatEnabled);
        nameField.setDisable(chatEnabled);
        listenPortField.setDisable(chatEnabled);

        startButton.setText(chatEnabled ? "Stop" : "Start");
    }

    private void appendSystem(String text) {
        Platform.runLater(() -> statusLabel.setText(formatter.formatStatus(text)));
    }

    private void appendChat(ChatMessage message) {
        UiLine line = new UiLine(UiLineType.CHAT, formatter.format(message));
        historyList.getItems().add(line);
        historyList.scrollTo(historyList.getItems().size() - 1);
    }

    private void shutdown() {
        PeerSessionManager manager = sessionManager;
        sessionManager = null;
        chatService = null;
        started = false;
        if (manager != null) {
            manager.shutdown();
        }
    }

    private enum UiLineType {
        CHAT,
        SYSTEM
    }

    private record UiLine(UiLineType type, String text) {
    }

    private static final class UiLineCellFactory implements Callback<ListView<UiLine>, ListCell<UiLine>> {
        @Override
        public ListCell<UiLine> call(ListView<UiLine> listView) {
            return new ListCell<>() {
                @Override
                protected void updateItem(UiLine item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        getStyleClass().remove("system");
                        return;
                    }
                    setText(item.text());
                    if (item.type() == UiLineType.SYSTEM) {
                        if (!getStyleClass().contains("system")) {
                            getStyleClass().add("system");
                        }
                    } else {
                        getStyleClass().remove("system");
                    }
                }
            };
        }
    }
}

