package player;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for TurnDecision -- focused on the scoring table
 * and basic data representation correctness.
 */
public class TurnDecisionTest {
 
    // helpers
 
    private static Card plainCard() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
    }
 
    private static Card starCard() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), true);
    }
 
    private static TurnDecision empty() {
        return new TurnDecision(
            List.of(), List.of(), 0, new Pebbles());
    }
 
    // score -- plain card
 
    @Test
    void plainCardWith3OrMorePebblesGives1() {
        assertEquals(1, TurnDecision.score(plainCard(), 3));
    }
 
    @Test
    void plainCardWith2PebblesGives2() {
        assertEquals(2, TurnDecision.score(plainCard(), 2));
    }
 
    @Test
    void plainCardWith1PebbleGives3() {
        assertEquals(3, TurnDecision.score(plainCard(), 1));
    }
 
    @Test
    void plainCardWith0PebblesGives5() {
        assertEquals(5, TurnDecision.score(plainCard(), 0));
    }
 
    // score -- starred card
 
    @Test
    void starCardWith3OrMorePebblesGives2() {
        assertEquals(2, TurnDecision.score(starCard(), 3));
    }
 
    @Test
    void starCardWith2PebblesGives3() {
        assertEquals(3, TurnDecision.score(starCard(), 2));
    }
 
    @Test
    void starCardWith1PebbleGives5() {
        assertEquals(5, TurnDecision.score(starCard(), 1));
    }
 
    @Test
    void starCardWith0PebblesGives8() {
        assertEquals(8, TurnDecision.score(starCard(), 0));
    }
 
    // accessors
 
    @Test
    void getPointsReturnsCorrectPoints() {
        TurnDecision d = new TurnDecision(
            List.of(), List.of(plainCard()), 3,
            new Pebbles(List.of(Pebble.RED)));
        assertEquals(3, d.getPoints());
    }
 
    @Test
    void getWalletReturnsCorrectWallet() {
        Pebbles wallet = new Pebbles(List.of(Pebble.BLUE, Pebble.GREEN));
        TurnDecision d = new TurnDecision(List.of(), List.of(), 0, wallet);
        assertEquals(wallet, d.getWallet());
    }
 
    @Test
    void getExchangesIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
            () -> empty().getExchanges().add(
                new ExchangeStep(
                    new Equation(
                        new Pebbles(List.of(Pebble.RED)),
                        new Pebbles(List.of(Pebble.BLUE))),
                    true)));
    }
 
    @Test
    void getPurchasesIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
            () -> empty().getPurchases().add(plainCard()));
    }
}