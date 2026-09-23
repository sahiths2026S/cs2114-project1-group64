import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

/** Launches Pill Roulette and validates optional terminal arguments. */
public class Main {
    /** Starts a game. Run --help for optional tutorial and demonstration modes. */
    public static void main(String[] args) {
        run(args, System.in, System.out);
    }

    /**
     * Testable entry point. Returns 0 normally or 2 for invalid options.
     * Streams remain owned by the caller and are never closed here.
     */
    public static int run(String[] args, InputStream in, PrintStream out) {
        Objects.requireNonNull(args, "Arguments cannot be null.");
        Objects.requireNonNull(in, "Input cannot be null.");
        Objects.requireNonNull(out, "Output cannot be null.");
        boolean tutorial = false;
        boolean demo = false;
        boolean help = false;
        Long seed = null;
        for (int i = 0; i < args.length; i++) {
            String option = args[i];
            if ("--help".equals(option)) {
                help = true;
            }
            else if ("--tutorial".equals(option)) {
                tutorial = true;
            }
            else if ("--demo".equals(option)) {
                demo = true;
            }
            else if ("--seed".equals(option)) {
                if (++i >= args.length) {
                    return invalidOptions(out, "--seed needs a whole number.");
                }
                try {
                    seed = Long.valueOf(args[i]);
                }
                catch (NumberFormatException exception) {
                    return invalidOptions(out, "--seed needs a valid 64-bit whole number.");
                }
            }
            else {
                return invalidOptions(out, "Unknown option: " + option);
            }
        }
        if (demo && seed != null) {
            return invalidOptions(out, "Choose either --demo or --seed, not both.");
        }
        if (help) {
            usage(out);
            return 0;
        }
        GameUi ui = new GameUi(out);
        out.println("Pill Roulette - two local players, one terminal.");
        if (tutorial) {
            ui.displayTutorial();
        }
        List<Pill> sequence;
        if (demo) {
            out.println("Demo mode: a fixed sequence for a repeatable presentation.");
            sequence = Arrays.asList(new Pill(true), new Pill(false),
                new Pill(true), new Pill(false), new Pill(true));
        }
        else {
            sequence = Game.shuffledPills(seed == null ? new Random() : new Random(seed));
        }
        Game game = new Game(sequence, new UserInput(new Scanner(in)), ui);
        game.play();
        return 0;
    }

    private static int invalidOptions(PrintStream out, String message) {
        out.println("Invalid option. " + message);
        usage(out);
        return 2;
    }

    private static void usage(PrintStream out) {
        out.println("Usage: java -cp build/classes Main [--tutorial] [--demo | --seed NUMBER]");
        out.println("       java -cp build/classes Main --help");
        out.println("During play: 1 = take pill, 2 = give pill. End input to exit.");
    }
}
