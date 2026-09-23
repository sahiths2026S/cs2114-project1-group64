import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class GameUiTest {
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final GameUi ui = new GameUi(new PrintStream(bytes));

    private Game game() {
        return new Game(Arrays.asList(new Pill(true), new Pill(false)),
            new UserInput(new Scanner("")), ui);
    }

    private String output() {
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    void constructorAcceptsStreamAndRejectsNull() {
        assertNotNull(ui);
        assertThrows(NullPointerException.class, () -> new GameUi(null));
    }

    @Test
    void displayShowsHealthCountsPercentagesTurnAndActions() {
        Game game = game();
        ui.display(game);
        String text = output();
        assertTrue(text.contains("Player 1 health: 3 [###]"));
        assertTrue(text.contains("Player 2 health: 3 [###]"));
        assertTrue(text.contains("cyanide 1 (50%)"));
        assertTrue(text.contains("sugar 1 (50%)"));
        assertTrue(text.contains("Player 1's turn"));
        assertTrue(text.contains("1 = take"));
        assertTrue(text.contains("2 = give"));
        assertEquals(1, game.remainingCyanide());
        assertEquals(1, game.remainingSugar());
    }

    @Test
    void displayFinishedGameDoesNotPromptOrChangeState() {
        Game game = game();
        game.getPlayer().takeDamage(3);
        ui.display(game);
        assertTrue(output().contains("Game finished."));
        assertFalse(output().contains("Choose 1 or 2"));
        assertEquals(0, game.getPlayer().getHealth());
        assertEquals(1, game.remainingCyanide());
        assertThrows(NullPointerException.class, () -> ui.display(null));
    }

    @Test
    void displayExhaustedGameDoesNotDivideByZero() {
        Game game = game();
        game.takePill();
        game.takePill();
        bytes.reset();
        ui.display(game);
        assertTrue(output().contains("cyanide 0 (0%), sugar 0 (0%)"));
        assertFalse(output().contains("NaN"));
        assertFalse(output().contains("Choose 1 or 2"));
    }

    @Test
    void invalidInputMessageCanRepeatWithoutMutatingGame() {
        Game game = game();
        ui.displayInvalidInput();
        ui.displayInvalidInput();
        assertTrue(output().contains("Enter exactly 1 (take) or 2 (give)"));
        assertEquals(2, output().split("Invalid input", -1).length - 1);
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(1, game.remainingCyanide());
        assertEquals(1, game.getCurrentPlayerNumber());
    }

    @Test
    void gameOverReportsEitherWinnerAndFinalHealth() {
        Game first = game();
        first.getOpponent().takeDamage(3);
        ui.displayGameOver(first);
        assertTrue(output().contains("Player 1 wins!"));
        assertTrue(output().contains("Player 2 health: 0 []"));
        bytes.reset();
        Game second = game();
        second.getPlayer().takeDamage(3);
        ui.displayGameOver(second);
        assertTrue(output().contains("Player 2 wins!"));
    }

    @Test
    void gameOverCalledEarlyIsInformationalAndRejectsNull() {
        Game game = game();
        ui.displayGameOver(game);
        assertTrue(output().contains("Game in progress."));
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(3, game.getOpponent().getHealth());
        assertEquals(1, game.getCurrentPlayerNumber());
        assertEquals(1, game.remainingCyanide());
        assertEquals(1, game.remainingSugar());
        assertThrows(NullPointerException.class, () -> ui.displayGameOver(null));
    }

    @Test
    void gameOverReportsExhaustionAsADraw() {
        Game game = game();
        game.takePill();
        game.takePill();
        bytes.reset();
        ui.displayGameOver(game);
        assertTrue(output().contains("Draw: no pills remain"));
    }

    @Test
    void pillResultDistinguishesTakeGiveSugarAndCyanide() {
        ui.displayPillResult(1, 1, false, 0);
        ui.displayPillResult(2, 1, true, 1);
        assertTrue(output().contains("Player 1 takes the pill."));
        assertTrue(output().contains("Sugar: Player 1 loses 0 health."));
        assertTrue(output().contains("Player 2 gives the pill to Player 1."));
        assertTrue(output().contains("Cyanide: Player 1 loses 1 health."));
    }

    @Test
    void pillResultRejectsInvalidPlayersAndNegativeDamageBeforePrinting() {
        assertThrows(IllegalArgumentException.class,
            () -> ui.displayPillResult(0, 1, true, 1));
        assertThrows(IllegalArgumentException.class,
            () -> ui.displayPillResult(1, 3, true, 1));
        assertThrows(IllegalArgumentException.class,
            () -> ui.displayPillResult(1, 2, true, -1));
        assertEquals("", output());
    }

    @Test
    void inputClosedCanBeDisplayedRepeatedly() {
        ui.displayInputClosed();
        ui.displayInputClosed();
        assertEquals(2, output().split("Input ended", -1).length - 1);
        assertTrue(output().contains("without requesting another command"));
    }

    @Test
    void tutorialExplainsRulesAndCanBeRepeatedWithoutChangingGame() {
        Game game = game();
        ui.displayTutorial();
        ui.displayTutorial();
        assertTrue(output().contains("remaining counts and percentages"));
        assertTrue(output().contains("Turns alternate"));
        assertTrue(output().contains("Sugar does not grant an extra turn"));
        assertTrue(output().contains("zero health loses"));
        assertTrue(output().contains("no real consumption"));
        assertEquals(2, output().split("HOW TO PLAY", -1).length - 1);
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(1, game.getCurrentPlayerNumber());
        assertEquals(1, game.remainingCyanide());
    }
}
