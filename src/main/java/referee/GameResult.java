package referee;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/*
 * The result of a completed game.
 *
 * Contains the names of the winners (players with the highest score)
 * and the names of the players that were eliminated for misbehaving.
 * There may be multiple winners if scores are tied at game end.
 *
 * Data representation:
 *   winners:    names of players with the highest score
 *   misbehaved: names of players eliminated during the game, in order
 */
public class GameResult {
 
    private final List<String> winners;
    private final List<String> misbehaved;
 
    // List<String> List<String> -> GameResult
    // creates a result with the given winner and misbehaved player names
    public GameResult(List<String> winners, List<String> misbehaved) {
        this.winners    = Collections.unmodifiableList(new ArrayList<>(winners));
        this.misbehaved = Collections.unmodifiableList(new ArrayList<>(misbehaved));
    }
 
    // GameResult -> List<String>
    // returns the names of the winning players
    public List<String> getWinners() {
        return winners;
    }
 
    // GameResult -> List<String>
    // returns the names of the misbehaving players in elimination order
    public List<String> getMisbehaved() {
        return misbehaved;
    }
 
    @Override
    public String toString() {
        return "Winners: " + winners + "  Misbehaved: " + misbehaved;
    }
}