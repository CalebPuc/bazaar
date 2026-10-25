package referee;
 
import common.*;
import player.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Referee class.
 *
 * Tests cover both cooperative scenarios (normal game play) and
 * non-cooperative ones (players that misbehave or throw exceptions).
 */
public class RefereeTest {
 
    // helpers
 
    private static Equations redForBlue() {
        return new Equations(List.of(
            new Equation(
                new Pebbles(List.of(Pebble.RED)),
                new Pebbles(List.of(Pebble.BLUE)))));
    }
 
    private static Card fiveRed() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
    }
 
    // -> GameState
    // one player with 5 RED, one visible card costing 5 RED, empty bank
    // player can buy the card immediately with no exchanges needed
    private static GameState onePlayerCanWin() {
        PlayerState p = new PlayerState(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), 0);
        return new GameState(
            new Pebbles(),
            new Cards(List.of(fiveRed())),
            new Cards(List.of()),
            List.of(p));
    }
 
    // -> GameState
    // two players, first has 5 RED and can buy immediately, bank is empty
    private static GameState twoPlayersFirstCanBuy() {
        PlayerState p1 = new PlayerState(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), 0);
        PlayerState p2 = new PlayerState(new Pebbles(), 0);
        return new GameState(
            new Pebbles(),
            new Cards(List.of(fiveRed())),
            new Cards(List.of()),
            List.of(p1, p2));
    }
 
    // -> GameState
    // two players both with RED, bank has BLUE, equation RED=BLUE exists
    // this gives the throwing strategy a chance to actually throw on
    // requestExchanges rather than being eliminated for a pebble request
    private static GameState twoPlayersWithExchanges() {
        PlayerState p1 = new PlayerState(
            new Pebbles(List.of(Pebble.RED, Pebble.RED)), 0);
        PlayerState p2 = new PlayerState(
            new Pebbles(List.of(Pebble.RED, Pebble.RED)), 0);
        return new GameState(
            new Pebbles(List.of(Pebble.BLUE, Pebble.BLUE)),
            new Cards(List.of(fiveRed())),
            new Cards(List.of()),
            List.of(p1, p2));
    }
 
    private static Mechanism pointsMech(String name) {
        return new Mechanism(name, new PurchasePointsStrategy());
    }
 
    // throws on any call to takeTurn -- simulates a completely broken player
    private static Strategy throwingStrategy() {
        return (turn, equations) -> {
            throw new RuntimeException("player broke");
        };
    }
 
    // a strategy that does nothing every turn -- used for stalemate testing
    private static Strategy doNothingStrategy() {
        return (turn, equations) -> new TurnDecision(
            new ArrayList<>(), new ArrayList<>(), 0,
            turn.getActive().getWallet());
    }
 
    // cooperative play
 
    @Test
    void gameWithOnePlayerProducesWinner() {
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(pointsMech("Alice")), onePlayerCanWin());
        assertFalse(result.getWinners().isEmpty());
    }
 
    @Test
    void winnerNameMatchesMechanism() {
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(pointsMech("Alice")), onePlayerCanWin());
        assertTrue(result.getWinners().contains("Alice"));
    }
 
    @Test
    void noMisbehavedPlayersInNormalGame() {
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(pointsMech("Alice")), onePlayerCanWin());
        assertTrue(result.getMisbehaved().isEmpty());
    }
 
    @Test
    void twoPlayerGameProducesAtLeastOneWinner() {
        Referee ref = new Referee(new Equations(List.of()));
        List<Mechanism> players = List.of(
            pointsMech("Alice"), pointsMech("Bob"));
        GameResult result = ref.runGame(players, twoPlayersFirstCanBuy());
        assertFalse(result.getWinners().isEmpty());
    }
 
    // non-cooperative: throwing player
 
    @Test
    void throwingPlayerIsEliminated() {
        // use a state where exchanges are possible so the strategy fires
        Referee ref = new Referee(redForBlue());
        Mechanism bad  = new Mechanism("Bad",  throwingStrategy());
        Mechanism good = new Mechanism("Good", new PurchasePointsStrategy());
        GameResult result = ref.runGame(
            List.of(bad, good), twoPlayersWithExchanges());
        assertTrue(result.getMisbehaved().contains("Bad"));
    }
 
    @Test
    void gameontinuesAfterBadPlayerEliminated() {
        Referee ref = new Referee(redForBlue());
        Mechanism bad  = new Mechanism("Bad",  throwingStrategy());
        Mechanism good = new Mechanism("Good", new PurchasePointsStrategy());
        GameResult result = ref.runGame(
            List.of(bad, good), twoPlayersWithExchanges());
        assertFalse(result.getWinners().isEmpty());
    }
 
    @Test
    void allMisbehavingPlayersProducesNoWinners() {
        // both players have exchanges available so the strategy fires and throws
        Referee ref = new Referee(redForBlue());
        Mechanism bad1 = new Mechanism("Bad1", throwingStrategy());
        Mechanism bad2 = new Mechanism("Bad2", throwingStrategy());
        GameResult result = ref.runGame(
            List.of(bad1, bad2), twoPlayersWithExchanges());
        assertTrue(result.getWinners().isEmpty());
        assertFalse(result.getMisbehaved().isEmpty());
    }
 
    // non-cooperative: setup misbehavior
 
    @Test
    void playerThatThrowsDuringSetupIsEliminated() {
        Mechanism bad = new Mechanism("Bad", new PurchasePointsStrategy()) {
            @Override
            public void setup(Equations equations) {
                throw new RuntimeException("setup failed");
            }
        };
        Mechanism good = pointsMech("Good");
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(bad, good), twoPlayersFirstCanBuy());
        assertTrue(result.getMisbehaved().contains("Bad"));
    }
 
    @Test
    void setupFailuresAreRecordedInTurnOrder() {
        Mechanism first = new Mechanism("First", new PurchasePointsStrategy()) {
            @Override
            public void setup(Equations equations) {
                throw new RuntimeException("setup failed");
            }
        };
        Mechanism second = new Mechanism("Second", new PurchasePointsStrategy()) {
            @Override
            public void setup(Equations equations) {
                throw new RuntimeException("setup failed");
            }
        };
        PlayerState empty = new PlayerState(new Pebbles(), 0);
        GameState start = new GameState(new Pebbles(), new Cards(List.of()),
            new Cards(List.of()), List.of(empty, empty));
        GameResult result = new Referee(new Equations(List.of()))
            .runGame(List.of(first, second), start);
        assertEquals(List.of("First", "Second"), result.getMisbehaved());
    }
 
    // non-cooperative: win notification misbehavior
 
    @Test
    void playerThatThrowsDuringWinMovesToDropouts() {
        // bad player wins the game but throws during win notification
        // so moves from winners to dropouts
        Mechanism bad = new Mechanism("Bad", new PurchasePointsStrategy()) {
            @Override
            public void win(boolean won) {
                throw new RuntimeException("win failed");
            }
        };
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(List.of(bad), onePlayerCanWin());
        assertFalse(result.getWinners().contains("Bad"));
        assertTrue(result.getMisbehaved().contains("Bad"));
    }
 
    // stalemate detection
 
    @Test
    void stalemateCounterTerminatesGameWhenNoProgressMade() {
        // both players do nothing every turn -- game should terminate
        // via stalemate counter rather than running forever
        Referee ref = new Referee(redForBlue());
        Mechanism p1 = new Mechanism("P1", doNothingStrategy());
        Mechanism p2 = new Mechanism("P2", doNothingStrategy());
        PlayerState ps1 = new PlayerState(
            new Pebbles(List.of(Pebble.RED)), 0);
        PlayerState ps2 = new PlayerState(
            new Pebbles(List.of(Pebble.RED)), 0);
        Card expensiveCard = new Card(new Pebbles(List.of(
            Pebble.BLUE, Pebble.BLUE, Pebble.BLUE,
            Pebble.BLUE, Pebble.BLUE)), false);
        GameState staleState = new GameState(
            new Pebbles(List.of(Pebble.BLUE, Pebble.BLUE)),
            new Cards(List.of(expensiveCard)),
            new Cards(List.of()),
            List.of(ps1, ps2));
        GameResult result = ref.runGame(List.of(p1, p2), staleState);
        assertNotNull(result);
        assertNotNull(result.getWinners());
    }
 
    @Test
    void stalemateCounterDoesNotFireInNormalGame() {
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(pointsMech("Alice")), onePlayerCanWin());
        assertTrue(result.getMisbehaved().isEmpty());
        assertFalse(result.getWinners().isEmpty());
    }
 
    // result structure
 
    @Test
    void gameResultWinnersIsNonNull() {
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(pointsMech("Alice")), onePlayerCanWin());
        assertNotNull(result.getWinners());
    }
 
    @Test
    void gameResultMisbehavedIsNonNull() {
        Referee ref = new Referee(new Equations(List.of()));
        GameResult result = ref.runGame(
            List.of(pointsMech("Alice")), onePlayerCanWin());
        assertNotNull(result.getMisbehaved());
    }
}