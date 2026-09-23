/** A participant's health, with validation at the boundary where it changes. */
public class Player {
    private int health;

    /**
     * Creates a player. Zero is allowed for representing a finished game.
     *
     * @param startingHealth initial health
     * @throws IllegalArgumentException if startingHealth is negative
     */
    public Player(int startingHealth) {
        if (startingHealth < 0) {
            throw new IllegalArgumentException("Starting health cannot be negative.");
        }
        health = startingHealth;
    }

    /** Reduces health without allowing negative health or negative damage. */
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        health = Math.max(0, health - amount);
    }

    /** Returns the current health. */
    public int getHealth() {
        return health;
    }
}
