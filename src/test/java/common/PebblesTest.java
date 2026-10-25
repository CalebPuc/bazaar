package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Pebbles class.
 *
 * Tests are organized by method. Each test covers one specific
 * behavior described in the purpose statement of the method.
 */
public class PebblesTest {
 
    // helpers
 
    // -> Pebbles
    // two RED pebbles
    private static Pebbles twoRed() {
        return new Pebbles(List.of(Pebble.RED, Pebble.RED));
    }
 
    // -> Pebbles
    // one BLUE pebble
    private static Pebbles oneBlue() {
        return new Pebbles(List.of(Pebble.BLUE));
    }
 
    // constructor
 
    @Test
    void emptyConstructorProducesEmptyCollection() {
        assertTrue(new Pebbles().isEmpty());
    }
 
    @Test
    void listConstructorCounts() {
        Pebbles p = new Pebbles(List.of(Pebble.RED, Pebble.RED, Pebble.BLUE));
        assertEquals(2, p.countOf(Pebble.RED));
        assertEquals(1, p.countOf(Pebble.BLUE));
    }
 
    // hasAtLeast
 
    @Test
    void hasAtLeastReturnsTrueWhenExact() {
        assertTrue(twoRed().hasAtLeast(twoRed()));
    }
 
    @Test
    void hasAtLeastReturnsTrueWhenMore() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED, Pebble.RED, Pebble.RED));
        assertTrue(wallet.hasAtLeast(twoRed()));
    }
 
    @Test
    void hasAtLeastReturnsFalseWhenNotEnough() {
        assertFalse(oneBlue().hasAtLeast(twoRed()));
    }
 
    @Test
    void hasAtLeastReturnsFalseWhenColorAbsent() {
        assertFalse(twoRed().hasAtLeast(oneBlue()));
    }
 
    @Test
    void hasAtLeastReturnsTrueForEmptyRequirement() {
        assertTrue(twoRed().hasAtLeast(new Pebbles()));
    }
 
    // size
 
    @Test
    void sizeReturnsCorrectTotal() {
        assertEquals(2, twoRed().size());
    }
 
    @Test
    void sizeOfEmptyIsZero() {
        assertEquals(0, new Pebbles().size());
    }
 
    // isEmpty
 
    @Test
    void isEmptyTrueForEmptyCollection() {
        assertTrue(new Pebbles().isEmpty());
    }
 
    @Test
    void isEmptyFalseForNonEmpty() {
        assertFalse(twoRed().isEmpty());
    }
 
    // isDisjointFrom
 
    @Test
    void disjointReturnsTrueWhenNoSharedColors() {
        assertTrue(twoRed().isDisjointFrom(oneBlue()));
    }
 
    @Test
    void disjointReturnsFalseWhenSharedColor() {
        assertFalse(twoRed().isDisjointFrom(twoRed()));
    }
 
    @Test
    void disjointReturnsTrueWhenOtherIsEmpty() {
        assertTrue(twoRed().isDisjointFrom(new Pebbles()));
    }
 
    // add
 
    @Test
    void addReturnsCombinedCollection() {
        Pebbles result = twoRed().add(oneBlue());
        assertEquals(2, result.countOf(Pebble.RED));
        assertEquals(1, result.countOf(Pebble.BLUE));
    }
 
    @Test
    void addDoesNotModifyOriginal() {
        Pebbles original = twoRed();
        original.add(oneBlue());
        assertEquals(2, original.size());
    }
 
    @Test
    void addWithEmptyReturnsSameContents() {
        assertEquals(twoRed(), twoRed().add(new Pebbles()));
    }
 
    // remove
 
    @Test
    void removeReturnsSmallerCollection() {
        Pebbles result = twoRed().remove(
            new Pebbles(List.of(Pebble.RED)));
        assertEquals(1, result.countOf(Pebble.RED));
    }
 
    @Test
    void removeDoesNotModifyOriginal() {
        Pebbles original = twoRed();
        original.remove(new Pebbles(List.of(Pebble.RED)));
        assertEquals(2, original.size());
    }
 
    @Test
    void removeThrowsWhenNotEnough() {
        assertThrows(IllegalArgumentException.class,
            () -> oneBlue().remove(twoRed()));
    }
 
    @Test
    void removeAllLeavesEmpty() {
        assertTrue(twoRed().remove(twoRed()).isEmpty());
    }
 
    // toList
 
    @Test
    void toListReturnsAllPebbles() {
        assertEquals(2, twoRed().toList().size());
    }
 
    @Test
    void toListIsInCanonicalOrder() {
        Pebbles mixed = new Pebbles(List.of(Pebble.BLUE, Pebble.RED));
        List<Pebble> list = mixed.toList();
        assertEquals(Pebble.RED,  list.get(0));
        assertEquals(Pebble.BLUE, list.get(1));
    }
 
    // equals and hashCode
 
    @Test
    void equalCollectionsAreEqual() {
        assertEquals(twoRed(), twoRed());
    }
 
    @Test
    void differentCollectionsAreNotEqual() {
        assertNotEquals(twoRed(), oneBlue());
    }
 
    @Test
    void equalCollectionsHaveSameHashCode() {
        assertEquals(twoRed().hashCode(), twoRed().hashCode());
    }
}