/**
 * Entry point for Pill Roulette. For now it just creates a game and prints
 * the opening text so we can see the project run end to end.
 *
 * @author group64
 * @version 2026.09.02
 */
public class Main {

    /**
     * Starts the game.
     *
     * @param args command line arguments, unused
     */
    public static void main(String[] args) {
        Roulette game = new Roulette();

        System.out.println("Welcome to Pill Roulette.");
        System.out.println("Press 1 to view tutorial, press 2 to skip.");
        System.out.println("There are " + game.cyanideCount
            + " cyanide pills and " + game.sugarCount + " sugar pills.");
        System.out.println("You have " + game.playerHealth
            + " health, and The Dealer has " + game.dealerHealth + " health.");
        System.out.println(
            "Press 1 to take a pill, press 2 to give opponent pill, "
            + "press 3 to use item.");
    }
}
