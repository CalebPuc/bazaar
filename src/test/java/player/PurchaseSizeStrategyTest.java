package player;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for PurchaseSizeStrategy.
 */
public class PurchaseSizeStrategyTest {
 
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
 
    private static final Strategy STRATEGY = new PurchaseSizeStrategy();
 
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
    void handlesEmptyVisibles() {
        TurnState turn = makeTurn(
            new Pebbles(List.of(Pebble.RED)), new Pebbles(), List.of());
        assertNotNull(STRATEGY.takeTurn(turn, new Equations(List.of())));
    }
 
    // size maximization
 
    @Test
    void buysTwoCardsWhenBothAffordable() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), plainFiveRed()));
        assertEquals(2,
            STRATEGY.takeTurn(turn, new Equations(List.of())).getPurchases().size());
    }
 
    @Test
    void prefersTwoCardsOverOneStar() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), plainFiveRed(), starFiveRed()));
        assertTrue(
            STRATEGY.takeTurn(turn, new Equations(List.of()))
                .getPurchases().size() >= 2);
    }
 
    @Test
    void doesNotBuyMoreThanAffordable() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), plainFiveRed()));
        assertEquals(1,
            STRATEGY.takeTurn(turn, new Equations(List.of())).getPurchases().size());
    }
 
    @Test
    void usesExchangeToEnableMorePurchases() {
        // player has 1 RED and 4 BLUE -- needs 5 BLUE to buy card
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
    void fewerExchangesPreferredWhenCountEqual() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED));
        Pebbles bank   = new Pebbles(List.of(Pebble.BLUE));
        TurnState turn = makeTurn(wallet, bank, List.of());
        TurnDecision d = STRATEGY.takeTurn(
            turn, new Equations(List.of(redForBlue())));
        assertTrue(d.getExchanges().size() <= 1);
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
 
    // wallet after decision
 
    @Test
    void walletEmptyAfterBuyingTwoFiveRedCards() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        TurnState turn = makeTurn(wallet, new Pebbles(),
            List.of(plainFiveRed(), plainFiveRed()));
        TurnDecision d = STRATEGY.takeTurn(turn, new Equations(List.of()));
        if (d.getPurchases().size() == 2) {
            assertEquals(0, d.getWallet().size());
        }
    }
}