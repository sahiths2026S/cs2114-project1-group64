/** Immutable identity of one fictional pill in the game. */
public class Pill {
    private final boolean cyanide;

    /** Creates a cyanide pill when true, or a sugar pill when false. */
    public Pill(boolean cyanide) {
        this.cyanide = cyanide;
    }

    /** Returns whether this pill causes cyanide damage. */
    public boolean isCyanide() {
        return cyanide;
    }
}
