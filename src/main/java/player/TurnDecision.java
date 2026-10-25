package player;
 
import common.Card;
import common.Pebbles;
import common.RuleBook;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/*
 * The decision a strategy makes for one turn: an ordered sequence
 * of exchanges followed by an ordered sequence of card purchases.
 *
 * Also records the total points earned and the player's wallet
 * after all moves, since the harness and referee both need these.
 *
 * Data representation:
 *   exchanges: ordered list of exchange steps to perform
 *   purchases: ordered list of cards to buy
 *   points:    total points earned from the purchases
 *   wallet:    the player's pebbles after exchanges and purchases
 */
public class TurnDecision {
 
    private final List<ExchangeStep> exchanges;
    private final List<Card>         purchases;
    private final int                points;
    private final Pebbles            wallet;
 
    // List<ExchangeStep> List<Card> int Pebbles -> TurnDecision
    // creates a TurnDecision with the given exchanges, purchases,
    // total points, and resulting wallet
    public TurnDecision(List<ExchangeStep> exchanges,
                        List<Card> purchases,
                        int points,
                        Pebbles wallet) {
        this.exchanges = Collections.unmodifiableList(new ArrayList<>(exchanges));
        this.purchases = Collections.unmodifiableList(new ArrayList<>(purchases));
        this.points    = points;
        this.wallet    = wallet;
    }
 
    // TurnDecision -> List<ExchangeStep>
    // returns the exchanges this decision makes, in order
    public List<ExchangeStep> getExchanges() {
        return exchanges;
    }
 
    // TurnDecision -> List<Card>
    // returns the cards this decision purchases, in order
    public List<Card> getPurchases() {
        return purchases;
    }
 
    // TurnDecision -> int
    // returns the total points earned by this decision's purchases
    public int getPoints() {
        return points;
    }
 
    // TurnDecision -> Pebbles
    // returns the player's wallet after all exchanges and purchases
    public Pebbles getWallet() {
        return wallet;
    }
 
    // Card int -> int
    // returns the points earned when purchasing the given card with
    // the given number of pebbles remaining in the wallet afterward
    // delegates to RuleBook so the scoring table is defined in one place
    public static int score(Card card, int pebblesRemaining) {
        return RuleBook.score(card, pebblesRemaining);
    }
 
    // TurnDecision -> String
    @Override
    public String toString() {
        return "TurnDecision{exchanges=" + exchanges.size()
            + ", purchases=" + purchases.size()
            + ", points=" + points + "}";
    }
}