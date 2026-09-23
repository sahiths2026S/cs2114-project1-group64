import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;

/** Plain ASCII terminal presentation; this class never changes game state. */
public class GameUi {
    private final PrintStream output;

    /** Creates a UI without taking ownership of the supplied output stream. */
    public GameUi(PrintStream output) {
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    /** Prints the public information for the current turn. */
    public void display(Game game) {
        Objects.requireNonNull(game, "Game cannot be null.");
        output.println();
        output.println("+--------------------------------------+");
        output.println("|             PILL ROULETTE            |");
        output.println("+--------------------------------------+");
        displayHealth(game);
        int cyanide = game.remainingCyanide();
        int sugar = game.remainingSugar();
        long total = (long)cyanide + sugar;
        double cyanidePercent = total == 0 ? 0.0 : 100.0 * cyanide / total;
        double sugarPercent = total == 0 ? 0.0 : 100.0 * sugar / total;
        output.printf(Locale.ROOT, "Remaining: cyanide %d (%.0f%%), sugar %d (%.0f%%)%n",
            cyanide, cyanidePercent, sugar, sugarPercent);
        if (game.isGameOver()) {
            output.println("Game finished.");
        }
        else if (game.isInputEnded()) {
            output.println("Input closed.");
        }
        else {
            output.println("Player " + game.getCurrentPlayerNumber() + "'s turn");
            output.println("1 = take the pill    2 = give it to the other player");
            output.print("Choose 1 or 2: ");
            output.flush();
        }
    }

    /** Explains accepted input after a malformed command. */
    public void displayInvalidInput() {
        output.println("Invalid input. Enter exactly 1 (take) or 2 (give).");
    }

    /** Prints a result, or a safe informational message if play is still active. */
    public void displayGameOver(Game game) {
        Objects.requireNonNull(game, "Game cannot be null.");
        output.println();
        displayHealth(game);
        int playerHealth = game.getPlayer().getHealth();
        int opponentHealth = game.getOpponent().getHealth();
        if (playerHealth == 0 && opponentHealth == 0) {
            output.println("Draw: both players have zero health.");
        }
        else if (playerHealth == 0) {
            output.println("Player 2 wins! Player 1 has zero health.");
        }
        else if (opponentHealth == 0) {
            output.println("Player 1 wins! Player 2 has zero health.");
        }
        else if (game.remainingCyanide() == 0 && game.remainingSugar() == 0) {
            output.println("Draw: no pills remain and both players survived.");
        }
        else if (game.isInputEnded()) {
            output.println("Input ended. Game stopped without a winner.");
        }
        else {
            output.println("Game in progress.");
        }
    }

    /** Prints a resolved action after the pill's identity can be revealed. */
    public void displayPillResult(int actorNumber, int recipientNumber,
        boolean cyanide, int damage) {
        if ((actorNumber != 1 && actorNumber != 2)
            || (recipientNumber != 1 && recipientNumber != 2)) {
            throw new IllegalArgumentException("Player numbers must be 1 or 2.");
        }
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        String action = actorNumber == recipientNumber ? "takes" : "gives";
        output.println("Player " + actorNumber + " " + action + " the pill"
            + (actorNumber == recipientNumber ? "." : " to Player " + recipientNumber + "."));
        output.println((cyanide ? "Cyanide" : "Sugar") + ": Player " + recipientNumber
            + " loses " + damage + " health.");
    }

    /** Indicates that the terminal input ended instead of requesting input forever. */
    public void displayInputClosed() {
        output.println("Input ended. Game stopped without requesting another command.");
    }

    /** Prints the rules and a short explanation of how public counts inform choices. */
    public void displayTutorial() {
        output.println("PILL ROULETTE - HOW TO PLAY");
        output.println("A fictional game for two local players sharing one terminal.");
        output.println("Player 1 starts. Enter 1 to take a pill or 2 to give it to the other player.");
        output.println("Cyanide costs the recipient 1 health; sugar costs no health.");
        output.println("Turns alternate after every valid action. Sugar does not grant an extra turn.");
        output.println("Watch the remaining counts and percentages: more cyanide means more risk.");
        output.println("The next pill stays hidden until an action resolves; update your prediction each turn.");
        output.println("A player at zero health loses. If no pills remain and both survive, it is a draw.");
        output.println("Invalid commands use no pill and do not change whose turn it is.");
        output.println("All pills are fictional game objects; no real consumption is involved.");
    }

    private void displayHealth(Game game) {
        output.println("Player 1 health: " + game.getPlayer().getHealth()
            + " " + healthBar(game.getPlayer().getHealth()));
        output.println("Player 2 health: " + game.getOpponent().getHealth()
            + " " + healthBar(game.getOpponent().getHealth()));
    }

    private String healthBar(int health) {
        // Keep output bounded even when a configured game uses very large health.
        int visible = Math.min(health, 10);
        return "[" + "#".repeat(visible) + (health > visible ? "+" : "") + "]";
    }
}
