import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/** Reads terminal lines and validates supported actions without parsing numbers. */
public class UserInput {
    private final Scanner scanner;

    /** Creates an input handler without taking ownership of the supplied scanner. */
    public UserInput(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
    }

    /**
     * Returns one complete line, including a blank line, or null when input ends.
     * A closed scanner is treated the same as end of input.
     */
    public String input() {
        try {
            return scanner.nextLine();
        }
        catch (NoSuchElementException | IllegalStateException exception) {
            return null;
        }
    }

    /** Accepts exactly 1 or 2, with optional surrounding whitespace. */
    public boolean isValidInput(String input) {
        if (input == null) {
            return false;
        }
        String command = input.trim();
        return command.equals("1") || command.equals("2");
    }

    /** Safely rejects non-string values arriving through a generic boundary. */
    public boolean isValidInput(Object input) {
        return input instanceof String && isValidInput((String)input);
    }
}
