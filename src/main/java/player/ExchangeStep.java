package player;
 
import common.Equation;
import common.Pebbles;
 
/*
 * One step in an exchange sequence: an equation applied in
 * a specific direction.
 *
 * Needed because an equation is bidirectional, so we have to
 * record which direction was used in each step.
 *
 * Data representation:
 *   equation:    the equation that was applied
 *   leftToRight: true if applied left-to-right, false if right-to-left
 */
public class ExchangeStep {
 
    private final Equation equation;
    private final boolean  leftToRight;
 
    // Equation boolean -> ExchangeStep
    // creates an exchange step for the given equation and direction
    // throws IllegalArgumentException if equation is null
    public ExchangeStep(Equation equation, boolean leftToRight) {
        if (equation == null) {
            throw new IllegalArgumentException("Equation must not be null.");
        }
        this.equation = equation;
        this.leftToRight = leftToRight;
    }
 
    // ExchangeStep -> Equation
    // returns the equation used in this step
    public Equation getEquation() {
        return equation;
    }
 
    // ExchangeStep -> boolean
    // true if this step applies the equation left-to-right
    public boolean isLeftToRight() {
        return leftToRight;
    }
 
    // ExchangeStep -> Pebbles
    // returns the pebbles the player gives to the bank in this step
    public Pebbles getGiven() {
        return leftToRight ? equation.getLeft() : equation.getRight();
    }
 
    // ExchangeStep -> Pebbles
    // returns the pebbles the player receives from the bank in this step
    public Pebbles getReceived() {
        return leftToRight ? equation.getRight() : equation.getLeft();
    }
 
    // ExchangeStep Object -> boolean
    // true if this step and that step use the same equation and direction
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeStep)) return false;
        ExchangeStep that = (ExchangeStep) o;
        return this.leftToRight == that.leftToRight
            && this.equation.equals(that.equation);
    }
 
    // ExchangeStep -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        return 31 * equation.hashCode() + (leftToRight ? 1 : 0);
    }
 
    // ExchangeStep -> String
    @Override
    public String toString() {
        String arrow = leftToRight ? "->" : "<-";
        return equation.getLeft() + " " + arrow + " " + equation.getRight();
    }
}