package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the TurnState class.
 */
public class TurnStateTest {
 
    // helpers
 
    private static Pebbles someBank() {
        return new Pebbles(List.of(Pebble.RED, Pebble.BLUE, Pebble.GREEN));
    }
 
    private static Cards someCards() {
        return new Cards(List.of(
            new Card(new Pebbles(List.of(
                Pebble.RED, Pebble.RED, Pebble.BLUE,
                Pebble.GREEN, Pebble.WHITE)), false)));
    }
 
    private static PlayerState someActive() {
        return new PlayerState(
            new Pebbles(List.of(Pebble.RED, Pebble.WHITE)), 3);
    }
 
    private static TurnState someTurnState() {
        return new TurnState(someBank(), someCards(), someActive(), List.of(5, 2));
    }
 
    // constructor
 
    @Test
    void constructorAcceptsValidArguments() {
        assertDoesNotThrow(() -> someTurnState());
    }
 
    @Test
    void constructorAcceptsEmptyScores() {
        assertDoesNotThrow(() ->
            new TurnState(someBank(), someCards(), someActive(), List.of()));
    }
 
    // accessors
 
    @Test
    void getBankReturnsBank() {
        assertEquals(someBank(), someTurnState().getBank());
    }
 
    @Test
    void getVisiblesReturnsCards() {
        assertEquals(someCards(), someTurnState().getVisibles());
    }
 
    @Test
    void getActiveReturnsActivePlayer() {
        assertEquals(someActive(), someTurnState().getActive());
    }
 
    @Test
    void getScoresReturnsScores() {
        assertEquals(List.of(5, 2), someTurnState().getScores());
    }
 
    @Test
    void getScoresIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
            () -> someTurnState().getScores().add(99));
    }
 
    // equals and hashCode
 
    @Test
    void equalTurnStatesAreEqual() {
        assertEquals(someTurnState(), someTurnState());
    }
 
    @Test
    void differentBanksNotEqual() {
        TurnState ts = new TurnState(
            new Pebbles(List.of(Pebble.YELLOW)),
            someCards(), someActive(), List.of(5, 2));
        assertNotEquals(someTurnState(), ts);
    }
 
    @Test
    void equalTurnStatesHaveSameHashCode() {
        assertEquals(someTurnState().hashCode(), someTurnState().hashCode());
    }
}