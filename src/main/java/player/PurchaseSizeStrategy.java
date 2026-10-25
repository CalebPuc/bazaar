package player;
 
import common.*;
 
/*
 * A strategy that maximizes the number of cards purchased in one turn.
 *
 * The search logic lives in AbstractStrategy. This class only defines
 * what "better" means -- more cards wins.
 */
public class PurchaseSizeStrategy extends AbstractStrategy {
 
    // TurnDecision TurnDecision -> boolean
    // true if 'a' buys more cards than 'b'
    @Override
    boolean isBetter(TurnDecision a, TurnDecision b) {
        return a.getPurchases().size() > b.getPurchases().size();
    }
}