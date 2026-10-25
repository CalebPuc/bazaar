package player;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the ExchangeStep class.
 */
public class ExchangeStepTest {
 
    // helpers
 
    private static Equation redForBlue() {
        return new Equation(
            new Pebbles(List.of(Pebble.RED)),
            new Pebbles(List.of(Pebble.BLUE)));
    }
 
    private static Equation twoWhiteForGreen() {
        return new Equation(
            new Pebbles(List.of(Pebble.WHITE, Pebble.WHITE)),
            new Pebbles(List.of(Pebble.GREEN)));
    }
 
    private static ExchangeStep ltr() {
        return new ExchangeStep(redForBlue(), true);
    }
 
    private static ExchangeStep rtl() {
        return new ExchangeStep(redForBlue(), false);
    }
 
    // constructor
 
    @Test
    void constructorAcceptsLeftToRight() {
        assertDoesNotThrow(() -> ltr());
    }
 
    @Test
    void constructorRejectsNullEquation() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExchangeStep(null, true));
    }
 
    // isLeftToRight
 
    @Test
    void isLeftToRightTrueForLTR() {
        assertTrue(ltr().isLeftToRight());
    }
 
    @Test
    void isLeftToRightFalseForRTL() {
        assertFalse(rtl().isLeftToRight());
    }
 
    // getGiven
 
    @Test
    void getGivenReturnsLeftSideForLTR() {
        assertEquals(new Pebbles(List.of(Pebble.RED)), ltr().getGiven());
    }
 
    @Test
    void getGivenReturnsRightSideForRTL() {
        assertEquals(new Pebbles(List.of(Pebble.BLUE)), rtl().getGiven());
    }
 
    // getReceived
 
    @Test
    void getReceivedReturnsRightSideForLTR() {
        assertEquals(new Pebbles(List.of(Pebble.BLUE)), ltr().getReceived());
    }
 
    @Test
    void getReceivedReturnsLeftSideForRTL() {
        assertEquals(new Pebbles(List.of(Pebble.RED)), rtl().getReceived());
    }
 
    @Test
    void givenAndReceivedAreOpposites() {
        assertEquals(ltr().getGiven(), rtl().getReceived());
        assertEquals(ltr().getReceived(), rtl().getGiven());
    }
 
    // equals and hashCode
 
    @Test
    void sameEquationAndDirectionAreEqual() {
        assertEquals(ltr(), ltr());
    }
 
    @Test
    void sameEquationDifferentDirectionNotEqual() {
        assertNotEquals(ltr(), rtl());
    }
 
    @Test
    void equalStepsHaveSameHashCode() {
        assertEquals(ltr().hashCode(), ltr().hashCode());
    }
}