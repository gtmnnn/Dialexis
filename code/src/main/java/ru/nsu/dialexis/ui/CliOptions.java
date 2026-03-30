package ru.nsu.dialexis.ui;

import java.util.Objects;

public record CliOptions(
        String userName,
        int listenPort,
        String connectHost,
        Integer connectPort) {

    public CliOptions {
        Objects.requireNonNull(userName);
    }

    /** Parses raw CLI arguments into validated application options. */
    public static CliOptions parse(String[] args) {
        String name = null;
        Integer port = null;
        String host = null;
        Integer remotePort = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--name" -> name = nextValue(args, ++i, "--name");
                case "--port" -> port = Integer.parseInt(nextValue(args, ++i, "--port"));
                case "--connect-host" -> host = nextValue(args, ++i, "--connect-host");
                case "--connect-port" -> remotePort = Integer.parseInt(nextValue(args, ++i, "--connect-port"));
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Argument --name is required");
        }
        if (port == null) {
            throw new IllegalArgumentException("Argument --port is required");
        }
        if ((host == null) != (remotePort == null)) {
            throw new IllegalArgumentException("Arguments --connect-host and --connect-port must be provided together");
        }

        return new CliOptions(name, port, host, remotePort);
    }

    /** Returns whether both remote peer arguments were provided. */
    public boolean hasRemotePeer() {
        return connectHost != null && connectPort != null;
    }

    private static String nextValue(String[] args, int index, String optionName) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Value is missing for " + optionName);
        }
        return args[index];
    }
}
