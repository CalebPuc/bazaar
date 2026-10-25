package common;
 
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the RuleBook class.
 *
 * Tests are organized by method. Each test covers one specific
 * behavior described in the purpose statement of the method.
 */
public class RuleBookTest {
 
    // helpers
 
    // -> TurnState
    // a basic turn state with RED in the wallet, BLUE in the bank,
    // and one card visible that costs 5 BLUE
    private static TurnState basicTurn() {
        Pebbles wallet = new Pebbles(List.of(Pebble.RED));
        Pebbles bank   = new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE));
        Card blueCard = new Card(new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE)), false);
        return new TurnState(bank, new Cards(List.of(blueCard)),
            new PlayerState(wallet, 0), List.of());
    }
 
    // -> Equations
    // one equation: RED = BLUE
    private static Equations redForBlueEq() {
        return new Equations(List.of(
            new Equation(
                new Pebbles(List.of(Pebble.RED)),
                new Pebbles(List.of(Pebble.BLUE)))));
    }
 
    // Pebble Pebble -> Pebbles[]
    // creates an exchange step as [given, received]
    private static Pebbles[] step(Pebble give, Pebble receive) {
        return new Pebbles[]{
            new Pebbles(List.of(give)),
            new Pebbles(List.of(receive))
        };
    }
 
    // -> List<Pebbles[]>
    // empty exchange list -- List.of() can't infer Pebbles[] type
    private static List<Pebbles[]> noExchanges() {
        return new ArrayList<>();
    }
 
    // score
 
    @Test
    void plainCardWith0PebblesGives5() {
        Card plain = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        assertEquals(5, RuleBook.score(plain, 0));
    }
 
    @Test
    void starCardWith0PebblesGives8() {
        Card star = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), true);
        assertEquals(8, RuleBook.score(star, 0));
    }
 
    @Test
    void plainCardWith3OrMorePebblesGives1() {
        Card plain = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        assertEquals(1, RuleBook.score(plain, 3));
        assertEquals(1, RuleBook.score(plain, 5));
    }
 
    // isLegalExchangeRequest
 
    @Test
    void emptyExchangeListIsLegal() {
        assertTrue(RuleBook.isLegalExchangeRequest(
            basicTurn(), noExchanges(), redForBlueEq()));
    }
 
    @Test
    void validSingleExchangeIsLegal() {
        List<Pebbles[]> exchanges = new ArrayList<>();
        exchanges.add(step(Pebble.RED, Pebble.BLUE));
        assertTrue(RuleBook.isLegalExchangeRequest(
            basicTurn(), exchanges, redForBlueEq()));
    }
 
    @Test
    void exchangeWithMoreThanFourStepsIsIllegal() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        Pebbles bank = new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE));
        TurnState turn = new TurnState(bank, new Cards(List.of()),
            new PlayerState(wallet, 0), List.of());
        List<Pebbles[]> exchanges = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            exchanges.add(step(Pebble.RED, Pebble.BLUE));
        }
        assertFalse(RuleBook.isLegalExchangeRequest(
            turn, exchanges, redForBlueEq()));
    }
 
    @Test
    void exchangeWhenWalletLacksGivenPebblesIsIllegal() {
        List<Pebbles[]> exchanges = new ArrayList<>();
        exchanges.add(step(Pebble.BLUE, Pebble.RED));
        assertFalse(RuleBook.isLegalExchangeRequest(
            basicTurn(), exchanges, redForBlueEq()));
    }
 
    @Test
    void exchangeNotMatchingAnyEquationIsIllegal() {
        List<Pebbles[]> exchanges = new ArrayList<>();
        exchanges.add(step(Pebble.RED, Pebble.GREEN));
        assertFalse(RuleBook.isLegalExchangeRequest(
            basicTurn(), exchanges, redForBlueEq()));
    }
 
    // isLegalPebbleRequest
 
    @Test
    void pebbleRequestLegalWhenBankNonEmptyAndNoExchangesPossible() {
        Pebbles wallet = new Pebbles(List.of(Pebble.YELLOW));
        Pebbles bank   = new Pebbles(List.of(Pebble.BLUE));
        TurnState turn = new TurnState(bank, new Cards(List.of()),
            new PlayerState(wallet, 0), List.of());
        assertTrue(RuleBook.isLegalPebbleRequest(turn, redForBlueEq()));
    }
 
    @Test
    void pebbleRequestIllegalWhenBankEmpty() {
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of()),
            new PlayerState(new Pebbles(List.of(Pebble.YELLOW)), 0),
            List.of());
        assertFalse(RuleBook.isLegalPebbleRequest(turn, redForBlueEq()));
    }
 
    @Test
    void pebbleRequestIllegalWhenExchangesArePossible() {
        assertFalse(RuleBook.isLegalPebbleRequest(basicTurn(), redForBlueEq()));
    }
 
    // isLegalPurchaseRequest
 
    @Test
    void emptyPurchaseListIsLegal() {
        assertTrue(RuleBook.isLegalPurchaseRequest(
            basicTurn(), List.of(), noExchanges()));
    }
 
    @Test
    void purchasingAffordableVisibleCardIsLegal() {
        Pebbles wallet = new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED));
        Card redCard = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of(redCard)),
            new PlayerState(wallet, 0), List.of());
        assertTrue(RuleBook.isLegalPurchaseRequest(
            turn, List.of(redCard), noExchanges()));
    }
 
    @Test
    void purchasingCardNotInVisiblesIsIllegal() {
        Card redCard = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of()),
            new PlayerState(new Pebbles(List.of(
                Pebble.RED, Pebble.RED, Pebble.RED,
                Pebble.RED, Pebble.RED)), 0),
            List.of());
        assertFalse(RuleBook.isLegalPurchaseRequest(
            turn, List.of(redCard), noExchanges()));
    }
 
    @Test
    void purchasingUnaffordableCardIsIllegal() {
        Card expensiveCard = new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
        TurnState turn = new TurnState(
            new Pebbles(), new Cards(List.of(expensiveCard)),
            new PlayerState(new Pebbles(List.of(
                Pebble.RED, Pebble.RED)), 0),
            List.of());
        assertFalse(RuleBook.isLegalPurchaseRequest(
            turn, List.of(expensiveCard), noExchanges()));
    }
 
    // pickPebble
 
    @Test
    void pickPebbleReturnsRedWhenAvailable() {
        Pebbles bank = new Pebbles(List.of(Pebble.RED, Pebble.BLUE));
        assertEquals(Pebble.RED, RuleBook.pickPebble(bank));
    }
 
    @Test
    void pickPebbleSkipsUnavailableColors() {
        Pebbles bank = new Pebbles(List.of(Pebble.BLUE, Pebble.GREEN));
        assertEquals(Pebble.BLUE, RuleBook.pickPebble(bank));
    }
 
    @Test
    void pickPebbleThrowsOnEmptyBank() {
        assertThrows(IllegalArgumentException.class,
            () -> RuleBook.pickPebble(new Pebbles()));
    }
}