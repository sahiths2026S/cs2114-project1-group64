import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/** Tests game rules using known pill sequences rather than random outcomes. */
public class GameTest {

    private static List<Pill> pills(boolean... cyanide) {
        List<Pill> result = new ArrayList<>();
        for (boolean value : cyanide) {
            result.add(new Pill(value));
        }
        return result;
    }

    /** Captures display calls without depending on the terminal's formatting. */
    private static class RecordingUi extends GameUi {
        private int activeDisplays;
        private int finalDisplays;
        private int invalidDisplays;

        RecordingUi() {
            super(new PrintStream(new ByteArrayOutputStream()));
        }

        @Override
        public void display(Game game) {
            activeDisplays++;
        }

        @Override
        public void displayGameOver(Game game) {
            finalDisplays++;
        }

        @Override
        public void displayInvalidInput() {
            invalidDisplays++;
        }
    }

    private static class Fixture {
        private final RecordingUi ui = new RecordingUi();
        private final UserInput input;
        private final Game game;

        Fixture(String commands, boolean... sequence) {
            input = new UserInput(new Scanner(commands));
            game = new Game(pills(sequence), input, ui);
        }
    }

    @Test
    void defaultGameStartsWithTwoHealthyPlayersAndTenPills() {
        Game game = new Game();
        assertAll(
            () -> assertEquals(3, game.getPlayer().getHealth()),
            () -> assertEquals(3, game.getOpponent().getHealth()),
            () -> assertNotSame(game.getPlayer(), game.getOpponent()),
            () -> assertSame(game.getPlayer(), game.getCurrentTurn()),
            () -> assertEquals(1, game.getCurrentPlayerNumber()),
            () -> assertEquals(5, game.remainingCyanide()),
            () -> assertEquals(5, game.remainingSugar()),
            () -> assertFalse(game.isGameOver()),
            () -> assertFalse(game.isInputEnded()));
    }

    @Test
    void configuredGameUsesRequestedHealthAndCopiesTheSequence() {
        List<Pill> sequence = pills(true, false);
        Game game = new Game(4, sequence,
            new UserInput(new Scanner("")), new RecordingUi());
        sequence.clear();
        assertEquals(4, game.getPlayer().getHealth());
        assertEquals(4, game.getOpponent().getHealth());
        assertEquals(1, game.remainingCyanide());
        assertEquals(1, game.remainingSugar());
    }

    @Test
    void constructorRejectsNonpositiveHealth() {
        for (int health : new int[] {0, -1, Integer.MIN_VALUE}) {
            assertThrows(IllegalArgumentException.class, () ->
                new Game(health, pills(true), new UserInput(new Scanner("")),
                    new RecordingUi()));
        }
    }

    @Test
    void constructorRejectsNullDependenciesAndNullPills() {
        UserInput input = new UserInput(new Scanner(""));
        RecordingUi ui = new RecordingUi();
        assertAll(
            () -> assertThrows(NullPointerException.class,
                () -> new Game(null, input, ui)),
            () -> assertThrows(NullPointerException.class,
                () -> new Game(pills(true), null, ui)),
            () -> assertThrows(NullPointerException.class,
                () -> new Game(pills(true), input, null)),
            () -> assertThrows(NullPointerException.class,
                () -> new Game(Arrays.asList(new Pill(true), null), input, ui)));
    }

    @Test
    void emptySequenceIsSafelyFinishedWithZeroCounts() {
        Fixture fixture = new Fixture("");
        assertTrue(fixture.game.isGameOver());
        assertEquals(0, fixture.game.remainingCyanide());
        assertEquals(0, fixture.game.remainingSugar());
        assertEquals(3, fixture.game.getPlayer().getHealth());
        assertEquals(3, fixture.game.getOpponent().getHealth());
    }

    @Test
    void takeCyanideDamagesActorRemovesOnePillAndSwitchesTurn() {
        Game game = new Fixture("", true, false, true).game;
        game.takePill();
        assertEquals(2, game.getPlayer().getHealth());
        assertEquals(3, game.getOpponent().getHealth());
        assertEquals(1, game.remainingCyanide());
        assertEquals(1, game.remainingSugar());
        assertSame(game.getOpponent(), game.getCurrentTurn());
        assertEquals(2, game.getCurrentPlayerNumber());
    }

    @Test
    void takeSugarDoesNotDamageEitherPlayer() {
        Game game = new Fixture("", false, true).game;
        game.takePill();
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(3, game.getOpponent().getHealth());
        assertEquals(0, game.remainingSugar());
        assertEquals(1, game.remainingCyanide());
        assertEquals(2, game.getCurrentPlayerNumber());
    }

    @Test
    void giveCyanideDamagesOtherPlayerAndSwitchesTurn() {
        Game game = new Fixture("", true, true, false).game;
        game.givePill();
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(2, game.getOpponent().getHealth());
        assertEquals(2, game.getCurrentPlayerNumber());
        game.givePill();
        assertEquals(2, game.getPlayer().getHealth());
        assertEquals(2, game.getOpponent().getHealth());
        assertEquals(1, game.getCurrentPlayerNumber());
        assertEquals(0, game.remainingCyanide());
        assertEquals(1, game.remainingSugar());
    }

    @Test
    void giveSugarDoesNotDamageEitherPlayer() {
        Game game = new Fixture("", false, true).game;
        game.givePill();
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(3, game.getOpponent().getHealth());
        assertEquals(0, game.remainingSugar());
        assertEquals(2, game.getCurrentPlayerNumber());
    }

    @Test
    void exhaustedTakeAndGiveAreSafeNoOps() {
        Fixture fixture = new Fixture("");
        assertDoesNotThrow(fixture.game::takePill);
        assertDoesNotThrow(fixture.game::givePill);
        assertEquals(3, fixture.game.getPlayer().getHealth());
        assertEquals(3, fixture.game.getOpponent().getHealth());
        assertEquals(1, fixture.game.getCurrentPlayerNumber());
        assertEquals(0, fixture.game.remainingCyanide());
        assertEquals(0, fixture.game.remainingSugar());
        assertTrue(fixture.ui.finalDisplays >= 1);
    }

    @Test
    void zeroHealthEndsGameAndBlocksFurtherActions() {
        Game game = new Game(1, pills(true, true, false),
            new UserInput(new Scanner("2\n")), new RecordingUi());
        game.takePill();
        assertTrue(game.isGameOver());
        assertEquals(0, game.getPlayer().getHealth());
        assertEquals(1, game.getCurrentPlayerNumber());
        game.takePill();
        game.givePill();
        game.startTurn();
        assertEquals(0, game.getPlayer().getHealth());
        assertEquals(1, game.getOpponent().getHealth());
        assertEquals(1, game.remainingCyanide());
        assertEquals(1, game.remainingSugar());
    }

    @Test
    void givingLethalPillEndsGameWithoutSwitchingTurn() {
        Game game = new Game(1, pills(true, false),
            new UserInput(new Scanner("")), new RecordingUi());
        game.givePill();
        assertTrue(game.isGameOver());
        assertEquals(0, game.getOpponent().getHealth());
        assertEquals(1, game.getPlayer().getHealth());
        assertEquals(1, game.getCurrentPlayerNumber());
        assertEquals(1, game.remainingSugar());
    }

    @Test
    void lastSugarEndsGameWithBothPlayersAlive() {
        Game game = new Fixture("", false).game;
        game.takePill();
        assertTrue(game.isGameOver());
        assertEquals(3, game.getPlayer().getHealth());
        assertEquals(3, game.getOpponent().getHealth());
        assertEquals(1, game.getCurrentPlayerNumber());
    }

    @Test
    void startTurnConsumesExactlyOneValidAction() {
        Fixture fixture = new Fixture("1\n2\n", true, false, true);
        fixture.game.startTurn();
        assertEquals(2, fixture.game.getPlayer().getHealth());
        assertEquals(3, fixture.game.getOpponent().getHealth());
        assertEquals(2, fixture.game.getCurrentPlayerNumber());
        assertEquals(1, fixture.game.remainingCyanide());
        assertEquals(1, fixture.game.remainingSugar());
        assertEquals("2", fixture.input.input());
    }

    @Test
    void startTurnRetriesMalformedInputWithoutSpendingExtraPills() {
        Fixture fixture = new Fixture("\n \nabc\n0.1\n-1\n12\n1a\n2\n",
            true, false);
        fixture.game.startTurn();
        assertEquals(3, fixture.game.getPlayer().getHealth());
        assertEquals(2, fixture.game.getOpponent().getHealth());
        assertEquals(0, fixture.game.remainingCyanide());
        assertEquals(1, fixture.game.remainingSugar());
        assertEquals(7, fixture.ui.invalidDisplays);
    }

    @Test
    void manyInvalidCommandsDoNotOverflowTheCallStack() {
        String commands = String.join("", Collections.nCopies(10000, "bad\n"))
            + "1\n";
        Fixture fixture = new Fixture(commands, false, true);
        assertDoesNotThrow(fixture.game::startTurn);
        assertEquals(10000, fixture.ui.invalidDisplays);
        assertEquals(0, fixture.game.remainingSugar());
        assertEquals(1, fixture.game.remainingCyanide());
    }

    @Test
    void startingAnEndedTurnDoesNotReadInput() {
        Fixture fixture = new Fixture("2\n");
        fixture.game.startTurn();
        assertEquals("2", fixture.input.input());
        assertTrue(fixture.game.isGameOver());
        assertTrue(fixture.ui.finalDisplays >= 1);
    }

    @Test
    void eofClosesInputWithoutChangingTheGameState() {
        Fixture fixture = new Fixture("", true, false);
        fixture.game.startTurn();
        assertTrue(fixture.game.isInputEnded());
        assertFalse(fixture.game.isGameOver());
        assertEquals(3, fixture.game.getPlayer().getHealth());
        assertEquals(3, fixture.game.getOpponent().getHealth());
        assertEquals(1, fixture.game.getCurrentPlayerNumber());
        assertEquals(1, fixture.game.remainingCyanide());
        assertEquals(1, fixture.game.remainingSugar());
        assertDoesNotThrow(fixture.game::startTurn);
        assertEquals(1, fixture.game.remainingCyanide());
    }

    @Test
    void invalidInputFollowedByEofPreservesHealthAndPills() {
        Fixture fixture = new Fixture("bad\n\n", true, false);
        fixture.game.play();
        assertTrue(fixture.game.isInputEnded());
        assertEquals(2, fixture.ui.invalidDisplays);
        assertEquals(3, fixture.game.getPlayer().getHealth());
        assertEquals(3, fixture.game.getOpponent().getHealth());
        assertEquals(1, fixture.game.remainingCyanide());
        assertEquals(1, fixture.game.remainingSugar());
    }

    @Test
    void displayShowsActiveStateWithoutReadingInputOrChangingState() {
        Fixture fixture = new Fixture("2\n", true, false);
        fixture.game.display();
        assertEquals(1, fixture.ui.activeDisplays);
        assertEquals(0, fixture.ui.finalDisplays);
        assertEquals("2", fixture.input.input());
        assertEquals(1, fixture.game.remainingCyanide());
        assertEquals(1, fixture.game.remainingSugar());
        assertEquals(3, fixture.game.getPlayer().getHealth());
    }

    @Test
    void displayShowsFinalStateForEmptyGameWithoutReadingInput() {
        Fixture fixture = new Fixture("1\n");
        fixture.game.display();
        assertEquals(0, fixture.ui.activeDisplays);
        assertEquals(1, fixture.ui.finalDisplays);
        assertEquals("1", fixture.input.input());
    }

    @Test
    void remainingCountsDescribeOnlyUnusedPills() {
        Game game = new Fixture("", true, false, false, true, false).game;
        assertEquals(2, game.remainingCyanide());
        assertEquals(3, game.remainingSugar());
        game.takePill();
        game.takePill();
        assertEquals(1, game.remainingCyanide());
        assertEquals(2, game.remainingSugar());
    }

    @Test
    void playRunsCompleteGameWithCorrectAlternatingTargets() {
        Fixture fixture = new Fixture("2\n1\n2\n1\n2\n",
            true, false, true, false, true);
        fixture.game.play();
        assertTrue(fixture.game.isGameOver());
        assertFalse(fixture.game.isInputEnded());
        assertEquals(3, fixture.game.getPlayer().getHealth());
        assertEquals(0, fixture.game.getOpponent().getHealth());
        assertEquals(0, fixture.game.remainingCyanide());
        assertEquals(0, fixture.game.remainingSugar());
        assertTrue(fixture.ui.finalDisplays >= 1);
    }

    @Test
    void playOnEmptySequenceSafelyDisplaysFinalState() {
        Fixture fixture = new Fixture("1\n");
        assertDoesNotThrow(fixture.game::play);
        assertTrue(fixture.game.isGameOver());
        assertEquals("1", fixture.input.input());
        assertTrue(fixture.ui.finalDisplays >= 1);
    }
}
