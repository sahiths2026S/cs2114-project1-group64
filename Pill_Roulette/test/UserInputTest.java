import static org.junit.jupiter.api.Assertions.*;

import java.util.Scanner;
import org.junit.jupiter.api.Test;

class UserInputTest {
    @Test
    void constructorAcceptsScannerAndRejectsNull() {
        assertNotNull(new UserInput(new Scanner("")));
        assertThrows(NullPointerException.class, () -> new UserInput(null));
    }

    @Test
    void inputReadsWholeLinesIncludingBlankAndUnterminatedLines() {
        UserInput input = new UserInput(new Scanner("1\n\n 2 "));
        assertEquals("1", input.input());
        assertEquals("", input.input());
        assertEquals(" 2 ", input.input());
        assertNull(input.input());
    }

    @Test
    void inputSafelyHandlesRepeatedEndOfInput() {
        UserInput input = new UserInput(new Scanner(""));
        assertNull(input.input());
        assertNull(input.input());
    }

    @Test
    void inputSafelyHandlesAClosedScanner() {
        Scanner scanner = new Scanner("1");
        UserInput input = new UserInput(scanner);
        scanner.close();
        assertNull(input.input());
        assertNull(input.input());
    }

    @Test
    void validatorAcceptsOnlyExactActionsWithSurroundingWhitespace() {
        UserInput input = new UserInput(new Scanner(""));
        for (String command : new String[] { "1", "2", " 1 ", "\t2\r" }) {
            assertTrue(input.isValidInput(command), command);
        }
    }

    @Test
    void validatorRejectsEverySpecifiedMalformedCommand() {
        UserInput input = new UserInput(new Scanner(""));
        assertFalse(input.isValidInput((String)null));
        for (String command : new String[] {
            "", " ", "\t", "0.1", "-1", "abc", "two", "12", "1a", "0", "3",
            "1 2", "+1", "01", "1.0", "999999999999999999999999999999999" }) {
            assertFalse(input.isValidInput(command), command);
        }
    }

    @Test
    void objectValidatorAcceptsStringsAndRejectsOtherTypesWithoutCasting() {
        UserInput input = new UserInput(new Scanner(""));
        assertTrue(input.isValidInput((Object)" 2 "));
        assertFalse(input.isValidInput((Object)"12"));
        for (Object value : new Object[] { null, 1, 2.0, true, new Object(), new char[] { '1' } }) {
            assertFalse(input.isValidInput(value));
        }
    }

    @Test
    void repeatedValidationDoesNotConsumeTheNextLine() {
        UserInput input = new UserInput(new Scanner("2\n"));
        assertFalse(input.isValidInput("bad"));
        assertFalse(input.isValidInput(""));
        assertEquals("2", input.input());
    }
}
