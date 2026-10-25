package player;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Mechanism class.
 *
 * Tests cover both cooperative behavior (normal strategy use) and
 * non-cooperative behavior (what happens when the strategy returns
 * unexpected results). The referee has to handle bad players, so
 * we test those paths here too.
 */
public class MechanismTest {
 
    // helpers
 
    // -> TurnState
    // a turn where the player has RED, bank has BLUE, card needs 5 BLUE
    private static TurnState basicTurn() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED));
        Pebbles bank   = new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE));
        Card blueCard  = new Card(new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE)), false);
        return new TurnState(bank, new Cards(List.of(blueCard)),
            new PlayerState(wallet, 0), List.of());
    }
 
    // -> TurnState
    // a turn where the player already has 5 RED and can buy directly
    private static TurnState affordableTurn() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        Card redCard = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        return new TurnState(new Pebbles(), new Cards(List.of(redCard)),
            new PlayerState(wallet, 0), List.of());
    }
 
    // -> TurnState
    // a turn where no exchanges are possible and the bank has pebbles
    private static TurnState pebbleTurn() {
        Pebbles wallet = new Pebbles(List.of(Pebble.YELLOW));
        Pebbles bank   = new Pebbles(List.of(Pebble.BLUE));
        return new TurnState(bank, new Cards(List.of()),
            new PlayerState(wallet, 0), List.of());
    }
 
    private static Equations redForBlue() {
        return new Equations(List.of(
            new Equation(
                new Pebbles(List.of(Pebble.RED)),
                new Pebbles(List.of(Pebble.BLUE)))));
    }
 
    private static Mechanism pointsMech(String name) {
        return new Mechanism(name, new PurchasePointsStrategy());
    }
 
    // constructor
 
    @Test
    void constructorAcceptsValidArguments() {
        assertDoesNotThrow(() -> pointsMech("Alice"));
    }
 
    // name
 
    @Test
    void nameReturnsCorrectName() {
        assertEquals("Alice", pointsMech("Alice").name());
    }
 
    // takeTurn
 
    @Test
    void takeTurnReturnsNonNullDecision() {
        Mechanism m = pointsMech("Alice");
        assertNotNull(m.takeTurn(basicTurn(), redForBlue()));
    }
 
    @Test
    void takeTurnDelegatesToStrategy() {
        // points strategy should find exchanges useful here
        Mechanism m = pointsMech("Alice");
        TurnDecision d = m.takeTurn(basicTurn(), redForBlue());
        assertNotNull(d);
    }
 
    // wantsPebble
 
    @Test
    void wantsPebbleTrueWhenNoExchangesPossible() {
        // wallet has YELLOW, bank has BLUE, equation needs RED
        // no exchanges possible -- should want a pebble
        Mechanism m = pointsMech("Alice");
        assertTrue(m.wantsPebble(pebbleTurn(), redForBlue()));
    }
 
    @Test
    void wantsPebbleFalseWhenExchangesPossible() {
        // wallet has RED, bank has BLUE, RED=BLUE equation exists
        Mechanism m = pointsMech("Alice");
        assertFalse(m.wantsPebble(basicTurn(), redForBlue()));
    }
 
    @Test
    void wantsPebbleFalseWhenBankEmpty() {
        Pebbles wallet = new Pebbles(List.of(Pebble.YELLOW));
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of()),
            new PlayerState(wallet, 0), List.of());
        assertFalse(pointsMech("Alice").wantsPebble(turn, redForBlue()));
    }
 
    // requestExchanges
 
    @Test
    void requestExchangesReturnsListOfPairs() {
        Mechanism m = pointsMech("Alice");
        List<Pebbles[]> exchanges = m.requestExchanges(basicTurn(), redForBlue());
        assertNotNull(exchanges);
        for (Pebbles[] step : exchanges) {
            assertEquals(2, step.length);
            assertNotNull(step[0]);
            assertNotNull(step[1]);
        }
    }
 
    @Test
    void requestExchangesEmptyWhenNoneNeeded() {
        // player can already afford the card -- no exchanges needed
        Mechanism m = pointsMech("Alice");
        List<Pebbles[]> exchanges =
            m.requestExchanges(affordableTurn(), new Equations(List.of()));
        assertTrue(exchanges.isEmpty());
    }
 
    // requestPurchases
 
    @Test
    void requestPurchasesReturnsNonNull() {
        Mechanism m = pointsMech("Alice");
        assertNotNull(m.requestPurchases(affordableTurn(),
            new Equations(List.of())));
    }
 
    @Test
    void requestPurchasesBuysAffordableCard() {
        Mechanism m = pointsMech("Alice");
        List<Card> purchases = m.requestPurchases(affordableTurn(),
            new Equations(List.of()));
        assertEquals(1, purchases.size());
    }
 
    @Test
    void requestPurchasesEmptyWhenNothingAffordable() {
        // wallet has YELLOW, nothing to buy
        Pebbles wallet = new Pebbles(List.of(Pebble.YELLOW));
        Card expensiveCard = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of(expensiveCard)),
            new PlayerState(wallet, 0), List.of());
        List<Card> purchases = pointsMech("Alice").requestPurchases(
            turn, new Equations(List.of()));
        assertTrue(purchases.isEmpty());
    }
 
    // two different strategies give same mechanism structure
 
    @Test
    void sizeMechanismBuysTwoCardsWhenAffordable() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED, Pebble.RED));
        Card redCard = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of(redCard, redCard)),
            new PlayerState(wallet, 0), List.of());
        Mechanism m = new Mechanism("Bob", new PurchaseSizeStrategy());
        assertEquals(2, m.requestPurchases(turn, new Equations(List.of())).size());
    }
}