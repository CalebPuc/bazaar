package common;
 
/*
 * The visible state of one player: their wallet and current score.
 *
 * Used in both TurnState (the player's own state) and GameState
 * (the referee's list of all players). Lives in common so neither
 * package has to import from the other.
 *
 * Data representation:
 *   wallet: the pebbles this player currently owns
 *   score:  this player's current point total (non-negative)
 */
public class PlayerState {
 
    private final Pebbles wallet;
    private final int score;
 
    // Pebbles int -> PlayerState
    // creates a PlayerState with the given wallet and score
    // throws IllegalArgumentException if score is negative
    public PlayerState(Pebbles wallet, int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score must be non-negative.");
        }
        this.wallet = wallet;
        this.score  = score;
    }
 
    // PlayerState -> Pebbles
    // returns this player's current pebble collection
    public Pebbles getWallet() {
        return wallet;
    }
 
    // PlayerState -> int
    // returns this player's current score
    public int getScore() {
        return score;
    }
 
    // PlayerState Pebbles -> PlayerState
    // returns a new PlayerState with the given wallet, same score
    // does NOT modify this 
    // used when the referee updates a player's pebbles
    public PlayerState withWallet(Pebbles newWallet) {
        return new PlayerState(newWallet, this.score);
    }
 
    // PlayerState int -> PlayerState
    // returns a new PlayerState with points added to the score, same wallet
    // does NOT modify this 
    // used when the referee awards points
    public PlayerState withAddedScore(int points) {
        return new PlayerState(this.wallet, this.score + points);
    }
 
    // PlayerState Object -> boolean
    // true if both states have the same wallet and score
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerState)) return false;
        PlayerState that = (PlayerState) o;
        return this.score == that.score
            && this.wallet.equals(that.wallet);
    }
 
    // PlayerState -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        return 31 * wallet.hashCode() + score;
    }
 
    // PlayerState -> String
    // text representation showing wallet and score
    // example: "Wallet: R R B  Score: 3"
    @Override
    public String toString() {
        return "Wallet: " + wallet.toString() + "  Score: " + score;
    }
}