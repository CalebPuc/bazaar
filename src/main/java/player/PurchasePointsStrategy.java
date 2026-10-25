package player;
 
import common.*;
 
/*
 * A strategy that maximizes the total points earned in one turn.
 *
 * The search logic lives in AbstractStrategy. This class only defines
 * what "better" means -- more points wins.
 */
public class PurchasePointsStrategy extends AbstractStrategy {
 
    // TurnDecision TurnDecision -> boolean
    // true if 'a' earns more points than 'b'
    @Override
    boolean isBetter(TurnDecision a, TurnDecision b) {
        return a.getPoints() > b.getPoints();
    }
}