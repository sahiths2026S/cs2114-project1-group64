import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data for one round of Pill Roulette: the health of both players
 * and the sequence of pills currently loaded in the chamber.
 *
 * @author group64
 * @version 2026.09.02
 */
public class Roulette {

    /** Health each side starts the game with. */
    static final int STARTING_HEALTH = 5;

    /** Health the player has left. */
    int playerHealth;

    /** Health the dealer has left. */
    int dealerHealth;

    /** The loaded chamber: 1 is a cyanide pill, 0 is a sugar pill. */
    List<Integer> pills;

    /** How many cyanide pills are in the chamber. */
    int cyanideCount;

    /** How many sugar pills are in the chamber. */
    int sugarCount;

    /** Items the player is holding. */
    List<String> playerItems;

    /** Items the dealer is holding. */
    List<String> dealerItems;

    /**
     * Creates a new game with both sides at full health and an empty chamber.
     */
    public Roulette() {
        playerHealth = STARTING_HEALTH;
        dealerHealth = STARTING_HEALTH;
        pills = new ArrayList<Integer>();
        cyanideCount = 0;
        sugarCount = 0;
        playerItems = new ArrayList<String>();
        dealerItems = new ArrayList<String>();
    }
}
