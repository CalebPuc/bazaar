package player;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Broad end-to-end tests covering both strategies across common
 * scenarios. Verifies that both strategies produce sensible decisions
 * without focusing on the specific goal of each.
 */
public class StrategyTest {
 
    // helpers
 
    private static Equation redForBlue() {
        return new Equation(
            new Pebbles(List.of(Pebble.RED)),
            new Pebbles(List.of(Pebble.BLUE)));
    }
 
    private static Card plainFiveRed() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
    }
 
    private static Card starFiveRed() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), true);
    }
 
    private static TurnState makeTurn(Pebbles wallet, Pebbles bank,
                                      List<Card> visibles) {
        return new TurnState(bank, new Cards(visibles),
            new PlayerState(wallet, 0), List.of());
    }
 
    // PurchasePointsStrategy
 
    @Test
    void pointsStrategyBuysAffordableCard() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(List.of(Pebble.BLUE)),
            List.of(plainFiveRed()));
        TurnDecision d = new PurchasePointsStrategy().takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertEquals(1, d.getPurchases().size());
        assertTrue(d.getPoints() > 0);
    }
 
    @Test
    void pointsStrategyPrefersStarCard() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), starFiveRed()));
        TurnDecision d = new PurchasePointsStrategy().takeTurn(
            turn, new Equations(List.of()));
        assertEquals(1, d.getPurchases().size());
        assertTrue(d.getPurchases().get(0).hasStar());
    }
 
    @Test
    void pointsStrategyReturnsZeroWhenNothingAffordable() {
        Pebbles wallet = new Pebbles(List.of(Pebble.YELLOW));
        TurnState turn = makeTurn(wallet, new Pebbles(List.of(Pebble.BLUE)),
            List.of(plainFiveRed()));
        TurnDecision d = new PurchasePointsStrategy().takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertEquals(0, d.getPurchases().size());
        assertEquals(0, d.getPoints());
    }
 
    @Test
    void pointsStrategyUsesExchangeToAffordCard() {
        // player has 1 RED and 4 BLUE -- needs 5 BLUE
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED,
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE, Pebble.BLUE));
        Pebbles bank  = new Pebbles(List.of(Pebble.BLUE));
        Card blueCard = new Card(new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE)), false);
        TurnState turn = makeTurn(wallet, bank, List.of(blueCard));
        TurnDecision d = new PurchasePointsStrategy().takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertFalse(d.getExchanges().isEmpty());
        assertFalse(d.getPurchases().isEmpty());
    }
 
    // PurchaseSizeStrategy
 
    @Test
    void sizeStrategyBuysMultipleCards() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), plainFiveRed()));
        TurnDecision d = new PurchaseSizeStrategy().takeTurn(
            turn, new Equations(List.of()));
        assertEquals(2, d.getPurchases().size());
    }
 
    @Test
    void sizeStrategyPrefersMoreCardsOverMorePoints() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), plainFiveRed(), starFiveRed()));
        TurnDecision d = new PurchaseSizeStrategy().takeTurn(
            turn, new Equations(List.of()));
        assertTrue(d.getPurchases().size() >= 2);
    }
 
    // exchange cap -- applies to both strategies
 
    @Test
    void neverExceedsFourExchanges() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        Pebbles bank = new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE));
        TurnState turn = makeTurn(wallet, bank, List.of());
        TurnDecision d = new PurchasePointsStrategy().takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertTrue(d.getExchanges().size() <= 4);
    }
}