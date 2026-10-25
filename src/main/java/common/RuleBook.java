package common;
 
import java.util.ArrayList;
import java.util.List;
 
/*
 * The rule book for the Bazaar game.
 *
 * Both the referee and the player mechanism use this to check
 * whether a player's requested actions are legal. It lives in
 * common so neither package has to import from the other.
 *
 * An exchange step is represented here as a pair of Pebbles:
 *   [given, received] - what the player gives to the bank and
 *   what the player receives from the bank.
 *
 * Data representation:
 *   RuleBook is stateless - all methods take the game state they
 *   need as arguments. There is nothing to store.
 */
public class RuleBook {

    // Scoring table: points awarded based on pebbles remaining after purchase.
    // Starred cards score higher than plain cards at every threshold.
    // To change the scoring table, update only these constants.
    private static final int PLAIN_MANY  = 1;  // 3+ pebbles remaining
    private static final int PLAIN_TWO   = 2;  // exactly 2 remaining
    private static final int PLAIN_ONE   = 3;  // exactly 1 remaining
    private static final int PLAIN_ZERO  = 5;  // 0 remaining
    private static final int STAR_MANY   = 2;  // 3+ pebbles remaining
    private static final int STAR_TWO    = 3;  // exactly 2 remaining
    private static final int STAR_ONE    = 5;  // exactly 1 remaining
    private static final int STAR_ZERO   = 8;  // 0 remaining

    // Card int -> int
    // returns the points earned when purchasing the given card with
    // the given number of pebbles remaining in the wallet afterward
    // moved here from TurnDecision since scoring is a rule, not a
    // property of a decision
    public static int score(Card card, int pebblesRemaining) {
        if (card.hasStar()) {
            if (pebblesRemaining >= 3) return STAR_MANY;
            if (pebblesRemaining == 2) return STAR_TWO;
            if (pebblesRemaining == 1) return STAR_ONE;
            return STAR_ZERO;
        } else {
            if (pebblesRemaining >= 3) return PLAIN_MANY;
            if (pebblesRemaining == 2) return PLAIN_TWO;
            if (pebblesRemaining == 1) return PLAIN_ONE;
            return PLAIN_ZERO;
        }
    }
 
    // TurnState List<Pebbles[]> Equations -> boolean
    // true if the given exchange sequence is a legal first action
    //
    // each exchange step is a Pebbles array of length 2:
    //   step[0] = pebbles the player gives to the bank
    //   step[1] = pebbles the player receives from the bank
    //
    // the sequence is legal if:
    //   - it has at most 4 steps
    //   - each step corresponds to a valid equation in either direction
    //   - each step is affordable given the running wallet and bank
    //
    // an empty list is always legal (player skips exchanges)
    public static boolean isLegalExchangeRequest(TurnState turn,
                                                  List<Pebbles[]> exchanges,
                                                  Equations equations) {
        if (exchanges.size() > 4) {
            return false;
        }
 
        Pebbles wallet = turn.getActive().getWallet();
        Pebbles bank   = turn.getBank();
 
        for (Pebbles[] step : exchanges) {
            Pebbles given    = step[0];
            Pebbles received = step[1];
 
            // player must have what they claim to give
            if (!wallet.hasAtLeast(given)) {
                return false;
            }
            // bank must have what the player claims to receive
            if (!bank.hasAtLeast(received)) {
                return false;
            }
            // the trade must correspond to a real equation
            if (!matchesEquation(given, received, equations)) {
                return false;
            }
 
            // apply the step
            wallet = wallet.remove(given).add(received);
            bank   = bank.remove(received).add(given);
        }
 
        return true;
    }
 
    // TurnState Equations -> boolean
    // true if the active player legally requesting a pebble from the bank
    //
    // legal only if:
    //   - the bank is non-empty, AND
    //   - no exchanges are possible with the current wallet and bank
    public static boolean isLegalPebbleRequest(TurnState turn,
                                                Equations equations) {
        if (turn.getBank().isEmpty()) {
            return false;
        }
        return equations.filterApplicable(
            turn.getActive().getWallet(), turn.getBank()).isEmpty();
    }
 
    // TurnState List<Card> List<Pebbles[]> -> boolean
    // true if the given card purchase list is a legal second action
    // after the given exchanges have been applied to the wallet
    //
    // legal if:
    //   - each card appears in the visible cards
    //   - each card is affordable given the wallet after prior purchases
    //
    // an empty list is always legal (player buys nothing)
    public static boolean isLegalPurchaseRequest(TurnState turn,
                                                  List<Card> purchases,
                                                  List<Pebbles[]> exchanges) {
        // compute wallet after exchanges
        Pebbles wallet = turn.getActive().getWallet();
        for (Pebbles[] step : exchanges) {
            wallet = wallet.remove(step[0]).add(step[1]);
        }
 
        // track remaining available cards
        List<Card> available = new ArrayList<>(turn.getVisibles().getCards());
 
        for (Card card : purchases) {
            if (!available.contains(card)) {
                return false;
            }
            if (!wallet.hasAtLeast(card.getPebbles())) {
                return false;
            }
            wallet = wallet.remove(card.getPebbles());
            available.remove(card);
        }
 
        return true;
    }
 
    // Pebbles -> Pebble
    // picks a pebble from the bank deterministically
    // iterates RED WHITE BLUE GREEN YELLOW and picks the first available
    // throws IllegalArgumentException if the bank is empty
    public static Pebble pickPebble(Pebbles bank) {
        for (Pebble p : Pebble.values()) {
            if (bank.countOf(p) > 0) {
                return p;
            }
        }
        throw new IllegalArgumentException("Cannot pick from an empty bank.");
    }
 
    // Pebbles Pebbles Equations -> boolean
    // true if [given, received] matches any equation in the table
    // in either direction
    private static boolean matchesEquation(Pebbles given,
                                           Pebbles received,
                                           Equations equations) {
        for (Equation eq : equations.getEquations()) {
            boolean ltr = eq.getLeft().equals(given)
                       && eq.getRight().equals(received);
            boolean rtl = eq.getRight().equals(given)
                       && eq.getLeft().equals(received);
            if (ltr || rtl) return true;
        }
        return false;
    }
}