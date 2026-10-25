package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Equation class.
 */
public class EquationTest {
 
    // helpers
 
    // -> Equation
    // a simple RED = BLUE equation
    private static Equation redBlue() {
        return new Equation(
            new Pebbles(List.of(Pebble.RED)),
            new Pebbles(List.of(Pebble.BLUE)));
    }
 
    // -> Pebbles two RED, one BLUE (can afford left side and receive right)
    private static Pebbles walletWithRed() {
        return new Pebbles(List.of(Pebble.RED, Pebble.RED));
    }
 
    private static Pebbles bankWithBlue() {
        return new Pebbles(List.of(Pebble.BLUE, Pebble.BLUE));
    }
 
    // constructor
 
    @Test
    void constructorAcceptsValidEquation() {
        assertDoesNotThrow(() -> redBlue());
    }
 
    @Test
    void constructorRejectsSharedColors() {
        assertThrows(IllegalArgumentException.class, () ->
            new Equation(
                new Pebbles(List.of(Pebble.RED)),
                new Pebbles(List.of(Pebble.RED))));
    }
 
    @Test
    void constructorRejectsSideWithMoreThanFourPebbles() {
        assertThrows(IllegalArgumentException.class, () ->
            new Equation(
                new Pebbles(List.of(Pebble.RED, Pebble.RED,
                    Pebble.RED, Pebble.RED, Pebble.RED)),
                new Pebbles(List.of(Pebble.BLUE))));
    }
 
    @Test
    void constructorRejectsEmptySide() {
        assertThrows(IllegalArgumentException.class, () ->
            new Equation(new Pebbles(), new Pebbles(List.of(Pebble.BLUE))));
    }
 
    // canApplyLeftToRight
 
    @Test
    void canApplyLeftToRightTrueWhenAffordable() {
        assertTrue(redBlue().canApplyLeftToRight(walletWithRed(), bankWithBlue()));
    }
 
    @Test
    void canApplyLeftToRightFalseWhenWalletMissing() {
        assertFalse(redBlue().canApplyLeftToRight(bankWithBlue(), bankWithBlue()));
    }
 
    @Test
    void canApplyLeftToRightFalseWhenBankMissing() {
        assertFalse(redBlue().canApplyLeftToRight(walletWithRed(), walletWithRed()));
    }
 
    // canApplyRightToLeft
 
    @Test
    void canApplyRightToLeftTrueWhenAffordable() {
        assertTrue(redBlue().canApplyRightToLeft(bankWithBlue(), walletWithRed()));
    }
 
    @Test
    void canApplyRightToLeftFalseWhenWalletMissing() {
        assertFalse(redBlue().canApplyRightToLeft(walletWithRed(), bankWithBlue()));
    }
 
    // canApply
 
    @Test
    void canApplyTrueIfEitherDirectionWorks() {
        assertTrue(redBlue().canApply(walletWithRed(), bankWithBlue()));
        assertTrue(redBlue().canApply(bankWithBlue(), walletWithRed()));
    }
 
    @Test
    void canApplyFalseWhenNeitherDirectionWorks() {
        assertFalse(redBlue().canApply(new Pebbles(), new Pebbles()));
    }
 
    // equals -- bidirectional
 
    @Test
    void equalsSymmetricForSameEquation() {
        Equation e1 = redBlue();
        Equation e2 = new Equation(
            new Pebbles(List.of(Pebble.BLUE)),
            new Pebbles(List.of(Pebble.RED)));
        assertEquals(e1, e2);
    }
 
    @Test
    void equalsReturnsFalseForDifferentEquation() {
        Equation e1 = redBlue();
        Equation e2 = new Equation(
            new Pebbles(List.of(Pebble.GREEN)),
            new Pebbles(List.of(Pebble.WHITE)));
        assertNotEquals(e1, e2);
    }
 
    @Test
    void hashCodeSameForBothOrientations() {
        Equation e1 = redBlue();
        Equation e2 = new Equation(
            new Pebbles(List.of(Pebble.BLUE)),
            new Pebbles(List.of(Pebble.RED)));
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}