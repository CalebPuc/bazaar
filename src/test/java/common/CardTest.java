package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Card class.
 */
public class CardTest {
 
    // helpers
 
    private static Pebbles fiveRed() {
        return new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
    }
 
    private static Card plainFiveRed() {
        return new Card(fiveRed(), false);
    }
 
    private static Card starFiveRed() {
        return new Card(fiveRed(), true);
    }
 
    // constructor
 
    @Test
    void constructorAcceptsExactlyFivePebbles() {
        assertDoesNotThrow(() -> plainFiveRed());
    }
 
    @Test
    void constructorRejectsFewerThanFivePebbles() {
        assertThrows(IllegalArgumentException.class, () ->
            new Card(new Pebbles(List.of(Pebble.RED, Pebble.RED)), false));
    }
 
    @Test
    void constructorRejectsMoreThanFivePebbles() {
        assertThrows(IllegalArgumentException.class, () ->
            new Card(new Pebbles(List.of(
                Pebble.RED, Pebble.RED, Pebble.RED,
                Pebble.RED, Pebble.RED, Pebble.RED)), false));
    }
 
    // canAcquire
 
    @Test
    void canAcquireTrueWhenWalletHasExactAmount() {
        assertTrue(plainFiveRed().canAcquire(fiveRed()));
    }
 
    @Test
    void canAcquireTrueWhenWalletHasMore() {
        Pebbles rich = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED));
        assertTrue(plainFiveRed().canAcquire(rich));
    }
 
    @Test
    void canAcquireFalseWhenWalletTooSmall() {
        assertFalse(plainFiveRed().canAcquire(
            new Pebbles(List.of(Pebble.RED, Pebble.RED))));
    }
 
    // hasStar
 
    @Test
    void hasStarTrueForStarCard() {
        assertTrue(starFiveRed().hasStar());
    }
 
    @Test
    void hasStarFalseForPlainCard() {
        assertFalse(plainFiveRed().hasStar());
    }
 
    // equals and hashCode
 
    @Test
    void equalCardsAreEqual() {
        assertEquals(plainFiveRed(), plainFiveRed());
    }
 
    @Test
    void plainAndStarNotEqual() {
        assertNotEquals(plainFiveRed(), starFiveRed());
    }
 
    @Test
    void equalCardsHaveSameHashCode() {
        assertEquals(plainFiveRed().hashCode(), plainFiveRed().hashCode());
    }
}