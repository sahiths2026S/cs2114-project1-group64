import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Exercises the real command-line entry point with in-memory terminal input. */
public class MainTest {

    private static class RunResult {
        private final int status;
        private final String output;

        RunResult(String commands, String... args) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            status = Main.run(args,
                new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes));
            output = bytes.toString(StandardCharsets.UTF_8);
        }
    }

    @Test
    void helpPrintsUsageAndReturnsSuccessfully() {
        RunResult result = new RunResult("", "--help");
        assertEquals(0, result.status);
        assertTrue(result.output.toLowerCase().contains("usage"));
        assertTrue(result.output.contains("--demo"));
        assertTrue(result.output.contains("--seed"));
    }

    @Test
    void defaultGameHandlesImmediateEof() {
        RunResult result = new RunResult("");
        assertEquals(0, result.status);
        assertTrue(result.output.contains("Pill Roulette"));
    }

    @Test
    void demoCanBePlayedThroughWithPredictableOutcome() {
        RunResult result = new RunResult("2\n1\n2\n1\n2\n", "--demo");
        assertEquals(0, result.status);
        assertTrue(result.output.toLowerCase().contains("demo"));
        assertTrue(result.output.toLowerCase().contains("cyanide"));
        assertTrue(result.output.toLowerCase().contains("sugar"));
        assertTrue(result.output.toLowerCase().contains("wins")
            || result.output.toLowerCase().contains("winner"));
    }

    @Test
    void demoSurvivesBadInputBeforeValidCommands() {
        RunResult result = new RunResult("\nabc\n0.1\n-1\n2\n1\n2\n1\n2\n",
            "--demo");
        assertEquals(0, result.status);
        assertTrue(result.output.toLowerCase().contains("invalid"));
        assertTrue(result.output.toLowerCase().contains("wins")
            || result.output.toLowerCase().contains("winner"));
    }

    @Test
    void tutorialExplainsRulesAndThenAcceptsEof() {
        RunResult result = new RunResult("", "--tutorial");
        assertEquals(0, result.status);
        assertTrue(result.output.toLowerCase().contains("how to play"));
        assertTrue(result.output.toLowerCase().contains("turns alternate"));
        assertTrue(result.output.toLowerCase().contains("next pill stays hidden"));
        assertTrue(result.output.toLowerCase().contains("cyanide"));
        assertTrue(result.output.toLowerCase().contains("sugar"));
        assertTrue(result.output.contains("1"));
        assertTrue(result.output.contains("2"));
    }

    @Test
    void sameSeedAndCommandsProduceSameGame() {
        String commands = "1\n2\n1\n2\n1\n2\n1\n2\n1\n2\n";
        RunResult first = new RunResult(commands, "--seed", "64");
        RunResult second = new RunResult(commands, "--seed", "64");
        assertEquals(0, first.status);
        assertEquals(0, second.status);
        assertEquals(first.output, second.output);
    }

    @Test
    void seedAcceptsFullLongRange() {
        for (String seed : new String[] {"0", "-1", "9223372036854775807",
            "-9223372036854775808"}) {
            RunResult result = new RunResult("", "--seed", seed);
            assertEquals(0, result.status, "Valid seed: " + seed);
        }
    }

    @Test
    void unknownOptionReturnsUsageErrorWithoutStartingGame() {
        RunResult result = new RunResult("", "--unknown");
        assertEquals(2, result.status);
        assertTrue(result.output.toLowerCase().contains("usage"));
    }

    @Test
    void missingSeedReturnsUsageError() {
        RunResult result = new RunResult("", "--seed");
        assertEquals(2, result.status);
        assertTrue(result.output.toLowerCase().contains("usage"));
    }

    @Test
    void malformedAndOutOfRangeSeedsReturnUsageErrors() {
        for (String seed : new String[] {"", "abc", "0.1", "1x",
            "9223372036854775808", "-9223372036854775809"}) {
            RunResult result = new RunResult("", "--seed", seed);
            assertEquals(2, result.status, "Invalid seed: " + seed);
            assertTrue(result.output.toLowerCase().contains("usage"));
        }
    }

    @Test
    void demoAndSeedCannotBeCombinedInEitherOrder() {
        RunResult first = new RunResult("", "--demo", "--seed", "64");
        RunResult second = new RunResult("", "--seed", "64", "--demo");
        assertEquals(2, first.status);
        assertEquals(2, second.status);
        assertTrue(first.output.toLowerCase().contains("usage"));
        assertTrue(second.output.toLowerCase().contains("usage"));
    }

    @Test
    void mainDelegatesToRunnableEntryPoint() {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(bytes));
            assertDoesNotThrow(() -> Main.main(new String[] {"--help"}));
        }
        finally {
            System.setOut(originalOutput);
        }
        assertTrue(bytes.toString(StandardCharsets.UTF_8)
            .toLowerCase().contains("usage"));
    }
}
