package player;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for PurchasePointsStrategy.
 */
public class PurchasePointsStrategyTest {
 
    // helpers
 
    private static TurnState makeTurn(Pebbles wallet, Pebbles bank,
                                      List<Card> visibles) {
        return new TurnState(bank, new Cards(visibles),
            new PlayerState(wallet, 0), List.of());
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
 
    private static Equation redForBlue() {
        return new Equation(
            new Pebbles(List.of(Pebble.RED)),
            new Pebbles(List.of(Pebble.BLUE)));
    }
 
    private static final Strategy STRATEGY = new PurchasePointsStrategy();
 
    // basic correctness
 
    @Test
    void returnsNonNullDecision() {
        TurnState turn = makeTurn(
            new Pebbles(List.of(Pebble.RED)), new Pebbles(), List.of());
        assertNotNull(STRATEGY.takeTurn(turn, new Equations(List.of())));
    }
 
    @Test
    void buysAffordableCardDirectly() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(), List.of(plainFiveRed()));
        assertEquals(1,
            STRATEGY.takeTurn(turn, new Equations(List.of())).getPurchases().size());
    }
 
    @Test
    void doesNotBuyUnaffordableCard() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(), List.of(plainFiveRed()));
        assertTrue(
            STRATEGY.takeTurn(turn, new Equations(List.of())).getPurchases().isEmpty());
    }
 
    @Test
    void earnsZeroWhenNothingAffordable() {
        Pebbles wallet = new Pebbles(List.of(Pebble.YELLOW));
        TurnState turn = makeTurn(wallet, new Pebbles(), List.of(plainFiveRed()));
        assertEquals(0,
            STRATEGY.takeTurn(turn, new Equations(List.of())).getPoints());
    }
 
    @Test
    void handlesEmptyVisibles() {
        TurnState turn = makeTurn(
            new Pebbles(List.of(Pebble.RED)), new Pebbles(), List.of());
        TurnDecision d = STRATEGY.takeTurn(turn, new Equations(List.of()));
        assertNotNull(d);
        assertTrue(d.getPurchases().isEmpty());
    }
 
    // points maximization
 
    @Test
    void prefersStarCardOverPlainWhenMorePoints() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), starFiveRed()));
        TurnDecision d = STRATEGY.takeTurn(turn, new Equations(List.of()));
        assertEquals(1, d.getPurchases().size());
        assertTrue(d.getPurchases().get(0).hasStar());
    }
 
    @Test
    void usesExchangeToEnableCardPurchase() {
        // player has 1 RED and 4 BLUE -- needs 5 BLUE to buy card
        // exchange RED->BLUE gives the fifth blue
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED,
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE, Pebble.BLUE));
        Pebbles bank   = new Pebbles(List.of(Pebble.BLUE));
        Card blueCard  = new Card(new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE)), false);
        TurnState turn = makeTurn(wallet, bank, List.of(blueCard));
        TurnDecision d = STRATEGY.takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertFalse(d.getExchanges().isEmpty());
        assertFalse(d.getPurchases().isEmpty());
    }
 
    // tie-breaking
 
    @Test
    void fewerExchangesPreferredWhenOutcomesEqual() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED));
        Pebbles bank   = new Pebbles(List.of(Pebble.BLUE));
        TurnState turn = makeTurn(wallet, bank, List.of());
        TurnDecision d = STRATEGY.takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertTrue(d.getExchanges().size() <= 1);
    }
 
    @Test
    void plainCardScoreCorrectAtZeroPebbles() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(), List.of(plainFiveRed()));
        TurnDecision d = STRATEGY.takeTurn(turn, new Equations(List.of()));
        if (!d.getPurchases().isEmpty()) assertEquals(5, d.getPoints());
    }
 
    // exchange cap
 
    @Test
    void neverExceedsFourExchanges() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        Pebbles bank = new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE, Pebble.BLUE));
        TurnState turn = makeTurn(wallet, bank, List.of());
        TurnDecision d = STRATEGY.takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertTrue(d.getExchanges().size() <= 4);
    }
}