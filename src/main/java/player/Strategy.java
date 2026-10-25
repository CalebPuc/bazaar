package player;
 
import common.Equations;
import common.TurnState;
 
/*
 * The interface all player strategies implement.
 *
 * A strategy takes a turn state and the equation table and returns
 * a decision describing which exchanges to make and which cards
 * to purchase. The player mechanism holds a strategy and calls it
 * each turn without caring which implementation it is.
 */
public interface Strategy {
 
    // Strategy TurnState Equations -> TurnDecision
    // returns the exchanges and purchases this strategy recommends
    // for the given turn state and available equations
    TurnDecision takeTurn(TurnState turn, Equations equations);
}