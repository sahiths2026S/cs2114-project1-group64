import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PillTest {
    @Test
    void cyanideConstructorAndGetterPreserveTrue() {
        Pill pill = new Pill(true);
        assertTrue(pill.isCyanide());
        assertTrue(pill.isCyanide());
    }

    @Test
    void sugarIsTheOtherBooleanBoundaryAndRemainsSugar() {
        Pill pill = new Pill(false);
        assertFalse(pill.isCyanide());
        assertFalse(pill.isCyanide());
    }
}
