package referee;
 
import common.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the GameState class.
 */
public class GameStateTest {
 
    // helpers
 
    private static Pebbles fullBank() {
        return new Pebbles(List.of(
            Pebble.RED, Pebble.WHITE, Pebble.BLUE,
            Pebble.GREEN, Pebble.YELLOW));
    }
 
    private static Card anyCard() {
        return new Card(new Pebbles(List.of(
            Pebble.RED, Pebble.RED, Pebble.RED,
            Pebble.RED, Pebble.RED)), false);
    }
 
    private static Cards fourVisibles() {
        return new Cards(List.of(anyCard(), anyCard(), anyCard(), anyCard()));
    }
 
    private static PlayerState playerAt(int score) {
        return new PlayerState(
            new Pebbles(List.of(Pebble.WHITE, Pebble.BLUE)), score);
    }
 
    private static GameState twoPlayerGame() {
        return new GameState(fullBank(), fourVisibles(),
            new Cards(List.of(anyCard())),
            List.of(playerAt(0), playerAt(0)));
    }
 
    // constructor
 
    @Test
    void constructorAcceptsValidState() {
        assertDoesNotThrow(() -> twoPlayerGame());
    }
 
    @Test
    void constructorAcceptsEmptyPlayerList() {
        // allowed so that eliminating the last player leaves a valid state
        assertDoesNotThrow(() ->
            new GameState(fullBank(), fourVisibles(),
                new Cards(List.of()), List.of()));
    }
 
    @Test
    void constructorMakesDefensiveCopyOfPlayers() {
        List<PlayerState> players = new ArrayList<>(
            List.of(playerAt(0), playerAt(0)));
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), players);
        players.add(playerAt(5));
        assertEquals(2, gs.getPlayers().size());
    }
 
    // getActivePlayer
 
    @Test
    void getActivePlayerReturnsFirstPlayer() {
        GameState gs = twoPlayerGame();
        assertEquals(gs.getPlayers().get(0), gs.getActivePlayer());
    }
 
    @Test
    void getActivePlayerThrowsWhenNoPlayers() {
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of());
        assertThrows(IllegalStateException.class, gs::getActivePlayer);
    }
 
    // isGameOver
 
    @Test
    void isGameOverFalseForActiveGame() {
        assertFalse(twoPlayerGame().isGameOver());
    }
 
    @Test
    void isGameOverTrueWhenPlayerReaches20Points() {
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(playerAt(20), playerAt(0)));
        assertTrue(gs.isGameOver());
    }
 
    @Test
    void isGameOverTrueWhenNoCardsRemain() {
        GameState gs = new GameState(fullBank(),
            new Cards(List.of()), new Cards(List.of()),
            List.of(playerAt(0)));
        assertTrue(gs.isGameOver());
    }
 
    @Test
    void isGameOverTrueWhenBankEmptyAndNoOneCanBuy() {
        GameState gs = new GameState(new Pebbles(), fourVisibles(),
            new Cards(List.of()), List.of(playerAt(0)));
        assertTrue(gs.isGameOver());
    }
 
    @Test
    void isGameOverTrueWhenPlayerListEmpty() {
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of());
        assertTrue(gs.isGameOver());
    }
 
    // toTurnState
 
    @Test
    void toTurnStateBankMatchesGameBank() {
        GameState gs = twoPlayerGame();
        assertEquals(gs.getBank(), gs.toTurnState().getBank());
    }
 
    @Test
    void toTurnStateScoresExcludesActivePlayer() {
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()),
            List.of(playerAt(3), playerAt(7), playerAt(11)));
        assertEquals(List.of(7, 11), gs.toTurnState().getScores());
    }
 
    @Test
    void toTurnStateScoresEmptyForSinglePlayer() {
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(playerAt(5)));
        assertTrue(gs.toTurnState().getScores().isEmpty());
    }
 
    @Test
    void toTurnStateThrowsWhenNoPlayers() {
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of());
        assertThrows(IllegalStateException.class, gs::toTurnState);
    }
 
    // withActivePlayerRemoved
 
    @Test
    void withActivePlayerRemovedReducesPlayerCount() {
        assertEquals(1,
            twoPlayerGame().withActivePlayerRemoved().getPlayers().size());
    }
 
    @Test
    void withActivePlayerRemovedRemovesFirstPlayer() {
        PlayerState p1 = playerAt(3);
        PlayerState p2 = playerAt(7);
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(p1, p2));
        assertEquals(p2, gs.withActivePlayerRemoved().getActivePlayer());
    }
 
    @Test
    void withActivePlayerRemovedReturnsEmptyStateForLastPlayer() {
        // had a null pointer here before -- fixed by allowing empty player list
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(playerAt(0)));
        GameState result = gs.withActivePlayerRemoved();
        assertNotNull(result);
        assertTrue(result.isGameOver());
        assertTrue(result.getPlayers().isEmpty());
    }
 
    @Test
    void withActivePlayerRemovedDoesNotModifyOriginal() {
        GameState gs = twoPlayerGame();
        gs.withActivePlayerRemoved();
        assertEquals(2, gs.getPlayers().size());
    }
 
    // withUpdatedActivePlayer
 
    @Test
    void withUpdatedActivePlayerReplacesFirstPlayer() {
        PlayerState updated = playerAt(10);
        assertEquals(updated,
            twoPlayerGame().withUpdatedActivePlayer(updated).getActivePlayer());
    }
 
    @Test
    void withUpdatedActivePlayerPreservesOtherPlayers() {
        PlayerState p1 = playerAt(0);
        PlayerState p2 = playerAt(5);
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(p1, p2));
        assertEquals(p2,
            gs.withUpdatedActivePlayer(playerAt(99)).getPlayers().get(1));
    }
 
    @Test
    void withUpdatedActivePlayerDoesNotModifyOriginal() {
        GameState gs = twoPlayerGame();
        gs.withUpdatedActivePlayer(playerAt(99));
        assertEquals(playerAt(0), gs.getActivePlayer());
    }
 
    // withRotatedPlayers
 
    @Test
    void withRotatedPlayersMakesSecondPlayerActive() {
        PlayerState p1 = playerAt(0);
        PlayerState p2 = playerAt(5);
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(p1, p2));
        assertEquals(p2, gs.withRotatedPlayers().getActivePlayer());
    }
 
    @Test
    void withRotatedPlayersSendsFirstPlayerToBack() {
        PlayerState p1 = playerAt(0);
        PlayerState p2 = playerAt(5);
        GameState gs = new GameState(fullBank(), fourVisibles(),
            new Cards(List.of()), List.of(p1, p2));
        assertEquals(p1, gs.withRotatedPlayers().getPlayers().get(1));
    }
 
    @Test
    void withRotatedPlayersPreservesPlayerCount() {
        assertEquals(2, twoPlayerGame().withRotatedPlayers().getPlayers().size());
    }
 
    // withBank
 
    @Test
    void withBankUpdatesBank() {
        Pebbles newBank = new Pebbles(List.of(Pebble.YELLOW));
        assertEquals(newBank, twoPlayerGame().withBank(newBank).getBank());
    }
 
    // withUpdatedCards
 
    @Test
    void withUpdatedCardsChangesVisibles() {
        Cards newVisibles = new Cards(List.of(anyCard()));
        assertEquals(newVisibles,
            twoPlayerGame()
                .withUpdatedCards(newVisibles, new Cards(List.of()))
                .getVisibles());
    }
 
    @Test
    void withUpdatedCardsChangesDeck() {
        Cards newDeck = new Cards(List.of(anyCard(), anyCard()));
        assertEquals(newDeck,
            twoPlayerGame()
                .withUpdatedCards(fourVisibles(), newDeck)
                .getDeck());
    }
 
    // equals and hashCode
 
    @Test
    void equalGameStatesAreEqual() {
        assertEquals(twoPlayerGame(), twoPlayerGame());
    }
 
    @Test
    void equalGameStatesHaveSameHashCode() {
        assertEquals(twoPlayerGame().hashCode(), twoPlayerGame().hashCode());
    }
}