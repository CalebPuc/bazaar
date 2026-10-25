package player;
 
import common.*;
 
import java.util.ArrayList;
import java.util.List;
 
/*
 * The player mechanism: wraps a strategy and handles the protocol
 * the referee uses to interact with a player.
 *
 * A mechanism has a name and a strategy. The referee calls it twice
 * per turn: once to get exchanges or a pebble request, and once to
 * get a list of cards to purchase.
 *
 * The mechanics are abstracted over a strategy: the mechanism does
 * not know or care which strategy it holds, just that it implements
 * the Strategy interface.
 *
 * Data representation:
 *   name:     this player's name, unique within a game
 *   strategy: the strategy used to make decisions each turn
 */
public class Mechanism {
 
    private final String name;
    private final Strategy strategy;
 
    // String Strategy -> Mechanism
    // creates a player mechanism with the given name and strategy
    public Mechanism(String name, Strategy strategy) {
        this.name     = name;
        this.strategy = strategy;
    }
 
    // Mechanism -> String
    // returns this player's name
    public String name() {
        return name;
    }

    // Mechanism Equations -> void
    // called by the referee before the game starts to inform the player
    // of the equations for this game
    // a well-behaved player just receives this -- a misbehaving one throws
    public void setup(Equations equations) {
        // well-behaved player does nothing special here
    }
 
    // Mechanism TurnState Equations -> TurnDecision
    // asks the strategy what to do this turn and returns its decision
    // the referee uses this to get the exchange sequence and card purchases
    //
    // the mechanism delegates entirely to the strategy: it does not
    // second-guess or validate the strategy's output here; that is the
    // referee's job via RuleBook
    public TurnDecision takeTurn(TurnState turn, Equations equations) {
        return strategy.takeTurn(turn, equations);
    }
 
    // Mechanism TurnState Equations -> boolean
    // true if this player should request a pebble from the bank
    // instead of performing exchanges
    //
    // by the rules, a player requests a pebble only if no exchanges
    // are possible AND the bank is non-empty
    public boolean wantsPebble(TurnState turn, Equations equations) {
        return RuleBook.isLegalPebbleRequest(turn, equations);
    }
 
    // Mechanism TurnState Equations -> List<Pebbles[]>
    // returns the exchange sequence this player wants to perform
    // as a list of [given, received] pairs: extracts the exchange
    // steps from the strategy's TurnDecision and converts them to
    // the Pebbles[] format RuleBook expects
    public List<Pebbles[]> requestExchanges(TurnState turn,
                                             Equations equations) {
        TurnDecision decision = strategy.takeTurn(turn, equations);
        return convertExchanges(decision.getExchanges());
    }
 
    // Mechanism TurnState List<Pebbles[]> -> List<Card>
    // returns the cards this player wants to purchase after the given
    // exchanges have been applied
    //
    // re-runs the strategy on the updated turn state, which already
    // reflects this turn's exchanges or pebble draw. The strategy is
    // given an empty equation table: the exchange phase is over, so it
    // must choose purchases the current wallet can afford as-is. Passing
    // the real equations here would let it plan further exchanges and
    // return cards the player cannot actually pay for.
    //
    // this matches the purchases of the strategy's original full-turn
    // plan: the best plan's purchases are the best purchases for the
    // wallet its exchanges produce.
    public List<Card> requestPurchases(TurnState turn, Equations equations) {
        TurnDecision decision = strategy.takeTurn(turn, NO_EXCHANGES);
        return new ArrayList<>(decision.getPurchases());
    }

    // an empty equation table, used once the exchange phase is over
    private static final Equations NO_EXCHANGES = new Equations(List.of());

    // Mechanism boolean -> void
    // called by the referee at game end to inform the player whether they won
    // a well-behaved player just receives this -- a misbehaving one throws
    public void win(boolean won) {
        // well-behaved player does nothing special here
    }
 
    // Mechanism -> String
    @Override
    public String toString() {
        return "Mechanism(" + name + ")";
    }
 
    // List<ExchangeStep> -> List<Pebbles[]>
    // converts the strategy's exchange steps to the [given, received]
    // format used by RuleBook: kept private since it is just a format
    // conversion between two internal representations
    private static List<Pebbles[]> convertExchanges(
            List<ExchangeStep> steps) {
        List<Pebbles[]> result = new ArrayList<>();
        for (ExchangeStep step : steps) {
            result.add(new Pebbles[]{step.getGiven(), step.getReceived()});
        }
        return result;
    }
}