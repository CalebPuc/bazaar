package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Equations class.
 */
public class EquationsTest {
 
    // helpers
 
    private static Equation redBlue() {
        return new Equation(
            new Pebbles(List.of(Pebble.RED)),
            new Pebbles(List.of(Pebble.BLUE)));
    }
 
    private static Equation greenWhite() {
        return new Equation(
            new Pebbles(List.of(Pebble.GREEN)),
            new Pebbles(List.of(Pebble.WHITE)));
    }
 
    private static Equations twoEquations() {
        return new Equations(List.of(redBlue(), greenWhite()));
    }
 
    // constructor
 
    @Test
    void constructorAcceptsValidTable() {
        assertDoesNotThrow(() -> twoEquations());
    }
 
    @Test
    void constructorAcceptsEmptyTable() {
        assertDoesNotThrow(() -> new Equations(List.of()));
    }
 
    @Test
    void constructorRejectsMoreThanTenEquations() {
        List<Equation> tooMany = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) tooMany.add(redBlue());
        assertThrows(IllegalArgumentException.class,
            () -> new Equations(tooMany));
    }
 
    // filterApplicable
 
    @Test
    void filterReturnsApplicableEquation() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED));
        Pebbles bank   = new Pebbles(List.of(Pebble.BLUE));
        Equations filtered = twoEquations().filterApplicable(wallet, bank);
        assertEquals(1, filtered.size());
    }
 
    @Test
    void filterReturnsEmptyWhenNoneApplicable() {
        Equations filtered = twoEquations().filterApplicable(
            new Pebbles(), new Pebbles());
        assertTrue(filtered.isEmpty());
    }
 
    @Test
    void filterReturnsAllWhenAllApplicable() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.GREEN));
        Pebbles bank = new Pebbles(List.of(
            Pebble.BLUE, Pebble.WHITE));
        assertEquals(2, twoEquations().filterApplicable(wallet, bank).size());
    }
 
    // size and isEmpty
 
    @Test
    void sizeReturnsCorrectCount() {
        assertEquals(2, twoEquations().size());
    }
 
    @Test
    void isEmptyTrueForEmptyTable() {
        assertTrue(new Equations(List.of()).isEmpty());
    }
 
    // equals and hashCode
 
    @Test
    void equalTablesAreEqual() {
        assertEquals(twoEquations(), twoEquations());
    }
 
    @Test
    void differentTablesAreNotEqual() {
        assertNotEquals(twoEquations(), new Equations(List.of(redBlue())));
    }
 
    @Test
    void equalTablesHaveSameHashCode() {
        assertEquals(twoEquations().hashCode(), twoEquations().hashCode());
    }
}