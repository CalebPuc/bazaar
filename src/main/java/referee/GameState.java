package referee;
 
import common.*;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/*
 * The referee's complete private knowledge about a running game.
 *
 * Never shared directly with players: the referee derives a
 * TurnState from it containing only what the active player is
 * allowed to know.
 *
 * Data representation:
 *   bank:    pebbles available for trading
 *   visibles: the four face-up cards players may purchase
 *   deck:    remaining face-down cards not yet revealed
 *   players: all active players in turn order, each with a wallet
 *            and score; the first player is the active player.
 *            may be empty if all players have been eliminated --
 *            isGameOver() catches this case.
 */
public class GameState {
 
    private final Pebbles           bank;
    private final Cards             visibles;
    private final Cards             deck;
    private final List<PlayerState> players;
 
    // Pebbles Cards Cards List<PlayerState> -> GameState
    // creates a GameState with the given fields
    // players may be empty, but that just means the game is already over
    public GameState(Pebbles bank,
                     Cards visibles,
                     Cards deck,
                     List<PlayerState> players) {
        this.bank     = bank;
        this.visibles = visibles;
        this.deck     = deck;
        this.players  = Collections.unmodifiableList(new ArrayList<>(players));
    }
 
    // GameState -> Pebbles
    // returns the bank's current pebble supply
    public Pebbles getBank() {
        return bank;
    }
 
    // GameState -> Cards
    // returns the face-up cards available for purchase
    public Cards getVisibles() {
        return visibles;
    }
 
    // GameState -> Cards
    // returns the face-down card deck
    public Cards getDeck() {
        return deck;
    }
 
    // GameState -> List<PlayerState>
    // returns all players in turn order: first player is the active one
    // may be empty if all players have been eliminated
    public List<PlayerState> getPlayers() {
        return players;
    }
 
    // GameState -> PlayerState
    // returns the active player: the first in turn order
    // throws IllegalStateException if there are no players left
    // the referee should check isGameOver() before calling this
    public PlayerState getActivePlayer() {
        if (players.isEmpty()) {
            throw new IllegalStateException(
                "No active player -- all players have been eliminated.");
        }
        return players.get(0);
    }
 
    // GameState -> boolean
    // true if the game has reached a terminal state
    // conditions: all players eliminated; a player has 20+ points;
    //             no cards remain; or bank is empty and no one can buy
    public boolean isGameOver() {
        if (players.isEmpty()) {
            return true;
        }
        for (PlayerState p : players) {
            if (p.getScore() >= 20) {
                return true;
            }
        }
        if (visibles.isEmpty() && deck.isEmpty()) {
            return true;
        }
        if (bank.isEmpty() && !anyPlayerCanBuyCard()) {
            return true;
        }
        return false;
    }
 
    // GameState -> TurnState
    // returns the TurnState for the active player: only what they
    // are allowed to know: bank, visible cards, their own state,
    // and the scores (not wallets) of remaining players
    // throws IllegalStateException if there are no players
    public TurnState toTurnState() {
        if (players.isEmpty()) {
            throw new IllegalStateException(
                "Cannot create TurnState -- no players remaining.");
        }
        PlayerState active = players.get(0);
        List<Integer> otherScores = new ArrayList<>();
        for (int i = 1; i < players.size(); i++) {
            otherScores.add(players.get(i).getScore());
        }
        return new TurnState(bank, visibles, active, otherScores);
    }
 
    // GameState -> GameState
    // returns a new GameState with the active player removed
    // their pebbles disappear from the game -- not returned to the bank
    // if this was the last player, returns a GameState with an empty
    // player list that isGameOver() will catch on the next check
    public GameState withActivePlayerRemoved() {
        List<PlayerState> remaining =
            new ArrayList<>(players.subList(1, players.size()));
        return new GameState(bank, visibles, deck, remaining);
    }
 
    // GameState PlayerState -> GameState
    // returns a new GameState with the active player replaced by the given state
    // used when the referee applies a legal wallet or score change
    public GameState withUpdatedActivePlayer(PlayerState updated) {
        List<PlayerState> newPlayers = new ArrayList<>(players);
        newPlayers.set(0, updated);
        return new GameState(bank, visibles, deck, newPlayers);
    }
 
    // GameState -> GameState
    // returns a new GameState with the active player moved to the back
    // used at the end of a legal turn to grant the next player their turn
    public GameState withRotatedPlayers() {
        List<PlayerState> rotated = new ArrayList<>(players);
        PlayerState first = rotated.remove(0);
        rotated.add(first);
        return new GameState(bank, visibles, deck, rotated);
    }
 
    // GameState Pebbles -> GameState
    // returns a new GameState with the given bank
    // used when the referee applies exchanges or a pebble draw
    public GameState withBank(Pebbles newBank) {
        return new GameState(newBank, visibles, deck, players);
    }
 
    // GameState Cards Cards -> GameState
    // returns a new GameState with updated visible cards and deck
    // used when the referee replaces purchased cards with new ones
    public GameState withUpdatedCards(Cards newVisibles, Cards newDeck) {
        return new GameState(bank, newVisibles, newDeck, players);
    }
 
    // GameState Object -> boolean
    // true if this and 'that' have the same bank, cards, deck, and players
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState)) return false;
        GameState that = (GameState) o;
        return this.bank.equals(that.bank)
            && this.visibles.equals(that.visibles)
            && this.deck.equals(that.deck)
            && this.players.equals(that.players);
    }
 
    // GameState -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        int result = bank.hashCode();
        result = 31 * result + visibles.hashCode();
        result = 31 * result + deck.hashCode();
        result = 31 * result + players.hashCode();
        return result;
    }

    // GameState -> String
    // text representation of the full game state for logging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Game State ===\n");
        sb.append("Bank:      ").append(bank.toString()).append("\n");
        sb.append("Visibles:  ").append(visibles.toString());
        sb.append("Deck size: ").append(deck.size()).append(" cards\n");
        if (players.isEmpty()) {
            sb.append("Players:   (none)\n");
        } else {
            sb.append("Players:\n");
            for (int i = 0; i < players.size(); i++) {
                String label = (i == 0) ? " (active)" : "";
                sb.append(String.format("  %d%s: %s%n",
                    i + 1, label, players.get(i).toString()));
            }
        }
        return sb.toString();
    }
 
    // true if any player can buy at least one visible card
    // used for the bank-empty game-over check
    private boolean anyPlayerCanBuyCard() {
        for (PlayerState p : players) {
            if (visibles.canAcquireAny(p.getWallet())) {
                return true;
            }
        }
        return false;
    }
}