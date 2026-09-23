import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PlayerTest {
    @Test
    void constructorPreservesStartingHealth() {
        assertEquals(3, new Player(3).getHealth());
    }

    @Test
    void constructorRejectsNegativeHealthAndAcceptsZero() {
        assertThrows(IllegalArgumentException.class, () -> new Player(-1));
        assertEquals(0, new Player(0).getHealth());
    }

    @Test
    void damageReducesHealthAndGetterReportsCurrentValue() {
        Player player = new Player(3);
        player.takeDamage(1);
        assertEquals(2, player.getHealth());
        player.takeDamage(0);
        assertEquals(2, player.getHealth());
    }

    @Test
    void negativeDamageCannotHealOrChangeHealth() {
        Player player = new Player(3);
        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
        assertEquals(3, player.getHealth());
    }

    @Test
    void excessiveAndRepeatedDamageLeaveHealthAtZero() {
        Player player = new Player(3);
        player.takeDamage(Integer.MAX_VALUE);
        assertEquals(0, player.getHealth());
        player.takeDamage(1);
        assertEquals(0, player.getHealth());
    }
}
