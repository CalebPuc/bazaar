package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Cards class.
 */
public class CardsTest {
 
    // helpers
 
    private static Card plainFiveRed() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
    }
 
    private static Cards oneCard() {
        return new Cards(List.of(plainFiveRed()));
    }
 
    private static Cards twoCards() {
        return new Cards(List.of(plainFiveRed(), plainFiveRed()));
    }
 
    // constructor
 
    @Test
    void constructorAcceptsEmptyList() {
        assertDoesNotThrow(() -> new Cards(List.of()));
    }
 
    @Test
    void constructorAcceptsNonEmptyList() {
        assertDoesNotThrow(() -> oneCard());
    }
 
    // canAcquireAny
 
    @Test
    void canAcquireAnyTrueWhenAffordable() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        assertTrue(oneCard().canAcquireAny(wallet));
    }
 
    @Test
    void canAcquireAnyFalseWhenNoneAffordable() {
        assertFalse(oneCard().canAcquireAny(new Pebbles()));
    }
 
    @Test
    void canAcquireAnyFalseForEmptyCollection() {
        Pebbles richWallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        assertFalse(new Cards(List.of()).canAcquireAny(richWallet));
    }
 
    // size and isEmpty
 
    @Test
    void sizeReturnsCorrectCount() {
        assertEquals(2, twoCards().size());
    }
 
    @Test
    void isEmptyTrueForEmptyCollection() {
        assertTrue(new Cards(List.of()).isEmpty());
    }
 
    // getFirst and removeFirst
 
    @Test
    void getFirstReturnsFirstCard() {
        assertEquals(plainFiveRed(), oneCard().getFirst());
    }
 
    @Test
    void getFirstThrowsOnEmpty() {
        assertThrows(IllegalStateException.class,
            () -> new Cards(List.of()).getFirst());
    }
 
    @Test
    void removeFirstReducesSizeByOne() {
        assertEquals(1, twoCards().removeFirst().size());
    }
 
    @Test
    void removeFirstDoesNotModifyOriginal() {
        Cards original = twoCards();
        original.removeFirst();
        assertEquals(2, original.size());
    }
 
    @Test
    void removeFirstThrowsOnEmpty() {
        assertThrows(IllegalStateException.class,
            () -> new Cards(List.of()).removeFirst());
    }
 
    // equals and hashCode
 
    @Test
    void equalCollectionsAreEqual() {
        assertEquals(oneCard(), oneCard());
    }
 
    @Test
    void differentCollectionsAreNotEqual() {
        assertNotEquals(oneCard(), twoCards());
    }
 
    @Test
    void equalCollectionsHaveSameHashCode() {
        assertEquals(oneCard().hashCode(), oneCard().hashCode());
    }
}