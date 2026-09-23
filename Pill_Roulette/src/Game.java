import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

/** Coordinates a two-player game without parsing input or formatting its UI. */
public class Game {
    public static final int STARTING_HEALTH = 3;
    public static final int CYANIDE_DAMAGE = 1;
    public static final int INITIAL_CYANIDE = 5;
    public static final int INITIAL_SUGAR = 5;

    private final Player player;
    private final Player opponent;
    private final Deque<Pill> pills;
    private final UserInput input;
    private final GameUi ui;
    private Player currentTurn;
    private boolean inputEnded;

    /** Creates the normal shuffled game using the terminal. */
    public Game() {
        this(shuffledPills(new Random()), new UserInput(new Scanner(System.in)),
            new GameUi(System.out));
    }

    /** Creates a game with a known sequence, useful for demos and tests. */
    public Game(List<Pill> sequence, UserInput input, GameUi ui) {
        this(STARTING_HEALTH, sequence, input, ui);
    }

    /**
     * Copies the sequence so the caller cannot change the hidden queue.
     * An empty sequence is a finished game; invalid dependencies are rejected.
     * @param startingHealth positive health for each player
     * @param sequence ordered pills, with no null entries
     * @param input command reader
     * @param ui output service
     */
    public Game(int startingHealth, List<Pill> sequence, UserInput input,
        GameUi ui) {
        if (startingHealth <= 0) {
            throw new IllegalArgumentException("Starting health must be positive.");
        }
        Objects.requireNonNull(sequence, "Pill sequence cannot be null.");
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.ui = Objects.requireNonNull(ui, "UI cannot be null.");
        this.pills = new ArrayDeque<>();
        for (Pill pill : sequence) {
            this.pills.addLast(Objects.requireNonNull(pill, "Pill cannot be null."));
        }
        player = new Player(startingHealth);
        opponent = new Player(startingHealth);
        currentTurn = player;
    }

    /** Runs the complete game with a loop, including safe end-of-input exit. */
    public void play() {
        if (isGameOver()) {
            display();
            return;
        }
        while (!isGameOver() && !inputEnded) {
            startTurn();
        }
    }

    /** Displays and resolves one valid turn, retrying mistakes without recursion. */
    public void startTurn() {
        if (isGameOver()) {
            display();
            return;
        }
        if (inputEnded) {
            return;
        }
        display();
        while (true) {
            String command = input.input();
            if (command == null) {
                inputEnded = true;
                ui.displayInputClosed();
                return;
            }
            if (!input.isValidInput(command)) {
                ui.displayInvalidInput();
                continue;
            }
            if ("1".equals(command.trim())) {
                takePill();
            }
            else {
                givePill();
            }
            return;
        }
    }

    /** Resolves the next pill for the player whose turn it is. */
    public void takePill() {
        resolvePill(currentTurn);
    }

    /** Resolves the next pill for the other player. */
    public void givePill() {
        resolvePill(currentTurn == player ? opponent : player);
    }

    private void resolvePill(Player recipient) {
        if (isGameOver()) {
            display();
            return;
        }
        if (inputEnded) {
            return;
        }
        int actorNumber = getCurrentPlayerNumber();
        Pill pill = pills.removeFirst();
        int damage = pill.isCyanide() ? CYANIDE_DAMAGE : 0;
        recipient.takeDamage(damage);
        ui.displayPillResult(actorNumber, recipient == player ? 1 : 2,
            pill.isCyanide(), damage);
        if (isGameOver()) {
            display();
        }
        else {
            currentTurn = currentTurn == player ? opponent : player;
        }
    }

    /** Prints state only; interaction stays in play/startTurn to avoid recursion. */
    public void display() {
        if (isGameOver()) {
            ui.displayGameOver(this);
        }
        else {
            ui.display(this);
        }
    }

    /** EOF is cancellation, separate from a win or an exhausted-queue draw. */
    public boolean isGameOver() {
        return player.getHealth() == 0 || opponent.getHealth() == 0
            || pills.isEmpty();
    }

    public int remainingCyanide() {
        int count = 0;
        for (Pill pill : pills) {
            if (pill.isCyanide()) {
                count++;
            }
        }
        return count;
    }

    public int remainingSugar() {
        return pills.size() - remainingCyanide();
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public Player getCurrentTurn() {
        return currentTurn;
    }

    public int getCurrentPlayerNumber() {
        return currentTurn == player ? 1 : 2;
    }

    public boolean isInputEnded() {
        return inputEnded;
    }

    /** Package-private creation helper keeps the public API small. */
    static List<Pill> shuffledPills(Random random) {
        List<Pill> sequence = new ArrayList<>();
        for (int i = 0; i < INITIAL_CYANIDE; i++) {
            sequence.add(new Pill(true));
        }
        for (int i = 0; i < INITIAL_SUGAR; i++) {
            sequence.add(new Pill(false));
        }
        Collections.shuffle(sequence, random);
        return sequence;
    }
}
