package referee;
 
import common.*;
import player.*;
 
import org.junit.jupiter.api.Test;
 
import java.util.List;
 
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Plays many complete games from random starting states using the
 * built-in strategies. These strategies only ever make legal moves,
 * so the referee should never eliminate one of them.
 *
 * This catches mismatches between what a player is asked for on
 * each call and what the referee checks, which single-call unit
 * tests can miss.
 */
public class FullGameTest {
 
    private static final int GAMES = 300;
 
    @Test
    void builtInStrategiesAreNeverEliminated() {
        for (int g = 0; g < GAMES; g++) {
            Referee ref = new Referee(Equations.createRandom());
            List<Mechanism> players = List.of(
                new Mechanism("points-1", new PurchasePointsStrategy()),
                new Mechanism("size-1",   new PurchaseSizeStrategy()),
                new Mechanism("points-2", new PurchasePointsStrategy()));
 
            GameResult result = ref.runGame(players);
 
            assertTrue(result.getMisbehaved().isEmpty(),
                "game " + g + " eliminated legal players: " + result);
        }
    }
 
    @Test
    void everyGameProducesAWinnerWhenNoOneIsEliminated() {
        for (int g = 0; g < GAMES; g++) {
            Referee ref = new Referee(Equations.createRandom());
            GameResult result = ref.runGame(List.of(
                new Mechanism("a", new PurchasePointsStrategy()),
                new Mechanism("b", new PurchaseSizeStrategy())));
 
            assertFalse(result.getWinners().isEmpty(),
                "game " + g + " ended with no winner: " + result);
        }
    }
}
