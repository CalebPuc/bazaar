package common;
 
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the PlayerState class.
 */
public class PlayerStateTest {
 
    // helpers
 
    private static Pebbles twoRed() {
        return new Pebbles(List.of(Pebble.RED, Pebble.RED));
    }
 
    private static Pebbles oneBlue() {
        return new Pebbles(List.of(Pebble.BLUE));
    }
 
    private static PlayerState playerAt(int score) {
        return new PlayerState(twoRed(), score);
    }
 
    // constructor
 
    @Test
    void constructorAcceptsZeroScore() {
        assertDoesNotThrow(() -> playerAt(0));
    }
 
    @Test
    void constructorRejectsNegativeScore() {
        assertThrows(IllegalArgumentException.class,
            () -> new PlayerState(twoRed(), -1));
    }
 
    // getWallet and getScore
 
    @Test
    void getWalletReturnsCorrectWallet() {
        assertEquals(twoRed(), new PlayerState(twoRed(), 5).getWallet());
    }
 
    @Test
    void getScoreReturnsCorrectScore() {
        assertEquals(7, playerAt(7).getScore());
    }
 
    // withWallet
 
    @Test
    void withWalletUpdatesWallet() {
        assertEquals(oneBlue(), playerAt(5).withWallet(oneBlue()).getWallet());
    }
 
    @Test
    void withWalletPreservesScore() {
        assertEquals(5, playerAt(5).withWallet(oneBlue()).getScore());
    }
 
    @Test
    void withWalletDoesNotModifyOriginal() {
        PlayerState original = playerAt(5);
        original.withWallet(oneBlue());
        assertEquals(twoRed(), original.getWallet());
    }
 
    // withAddedScore
 
    @Test
    void withAddedScoreIncreasesScore() {
        assertEquals(8, playerAt(5).withAddedScore(3).getScore());
    }
 
    @Test
    void withAddedScorePreservesWallet() {
        assertEquals(twoRed(), playerAt(5).withAddedScore(3).getWallet());
    }
 
    @Test
    void withAddedScoreDoesNotModifyOriginal() {
        PlayerState original = playerAt(5);
        original.withAddedScore(3);
        assertEquals(5, original.getScore());
    }
 
    // equals and hashCode
 
    @Test
    void equalStatesAreEqual() {
        assertEquals(playerAt(5), playerAt(5));
    }
 
    @Test
    void differentScoresNotEqual() {
        assertNotEquals(playerAt(3), playerAt(5));
    }
 
    @Test
    void equalStatesHaveSameHashCode() {
        assertEquals(playerAt(5).hashCode(), playerAt(5).hashCode());
    }
}