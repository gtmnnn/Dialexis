package ru.nsu.dialexis.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CliOptionsTest {
    @Test
    void parsesValidArguments() {
        CliOptions options = CliOptions.parse(new String[] {
                "--name", "alice",
                "--port", "5000",
                "--connect-host", "127.0.0.1",
                "--connect-port", "5001"
        });

        assertEquals("alice", options.userName());
        assertEquals(5000, options.listenPort());
        assertEquals("127.0.0.1", options.connectHost());
        assertEquals(5001, options.connectPort());
        assertTrue(options.hasRemotePeer());
    }

    @Test
    void rejectsMissingName() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--port", "5000"
        }));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--name", "   ",
                "--port", "5000"
        }));
    }

    @Test
    void rejectsMissingPort() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--name", "alice"
        }));
    }

    @Test
    void rejectsUnknownArgument() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--name", "alice",
                "--port", "5000",
                "--wat", "x"
        }));
    }

    @Test
    void rejectsMissingValueForOption() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--name"
        }));
    }

    @Test
    void rejectsIncompleteRemotePeerArguments() {
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--name", "alice",
                "--port", "5000",
                "--connect-host", "127.0.0.1"
        }));

        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[] {
                "--name", "alice",
                "--port", "5000",
                "--connect-port", "5001"
        }));
    }
}
