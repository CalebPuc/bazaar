package common;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/*
 * The information the referee shares with the active player at
 * the start of their turn: only what the player is allowed to know.
 *
 * Does NOT include other players' wallets, only their scores.
 * Does NOT include the face-down deck.
 *
 * Data representation:
 *   bank:    the bank's current pebble supply
 *   visibles: the face-up cards available for purchase
 *   active:  the active player's own wallet and score
 *   scores:  the scores of the remaining players in turn order
 *            (does not include the active player's score)
 */
public class TurnState {
 
    private final Pebbles       bank;
    private final Cards         visibles;
    private final PlayerState   active;
    private final List<Integer> scores;
 
    // Pebbles Cards PlayerState List<Integer> -> TurnState
    // creates a TurnState with the given bank, visible cards,
    // active player state, and remaining player scores
    public TurnState(Pebbles bank,
                     Cards visibles,
                     PlayerState active,
                     List<Integer> scores) {
        this.bank     = bank;
        this.visibles = visibles;
        this.active   = active;
        this.scores   = Collections.unmodifiableList(new ArrayList<>(scores));
    }
 
    // TurnState -> Pebbles
    // returns the bank's current pebble supply
    public Pebbles getBank() {
        return bank;
    }
 
    // TurnState -> Cards
    // returns the face-up cards available for purchase
    public Cards getVisibles() {
        return visibles;
    }
 
    // TurnState -> PlayerState
    // returns the active player's wallet and score
    public PlayerState getActive() {
        return active;
    }
 
    // TurnState -> List<Integer>
    // returns the scores of the remaining players in turn order
    // does not include the active player's score
    public List<Integer> getScores() {
        return scores;
    }
 
    // TurnState Object -> boolean
    // true if both states have the same bank, cards, active player, and scores
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TurnState)) return false;
        TurnState that = (TurnState) o;
        return this.bank.equals(that.bank)
            && this.visibles.equals(that.visibles)
            && this.active.equals(that.active)
            && this.scores.equals(that.scores);
    }
 
    // TurnState -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        int result = bank.hashCode();
        result = 31 * result + visibles.hashCode();
        result = 31 * result + active.hashCode();
        result = 31 * result + scores.hashCode();
        return result;
    }
 
    // TurnState -> String
    // text representation showing the bank, cards, active player, and scores
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Turn State ===\n");
        sb.append("Bank:    ").append(bank.toString()).append("\n");
        sb.append("Cards:   ").append(visibles.toString()).append("\n");
        sb.append("Active:  ").append(active.toString()).append("\n");
        sb.append("Scores:  ").append(scores.toString()).append("\n");
        return sb.toString();
    }
}