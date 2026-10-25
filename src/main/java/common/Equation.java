package common;
 
/*
 * A single equation in the Bazaar game: a bidirectional trade
 * between two collections of pebbles.
 *
 * Either side can be given to receive the other, subject to what
 * the player and bank each have available.
 *
 * Data representation:
 *   left:  a Pebbles with 1-4 total pebbles
 *   right: a Pebbles with 1-4 total pebbles, disjoint colors from left
 *
 * Invariant: left and right are non-null, each has 1-4 pebbles,
 *            and they share no pebble colors.
 */
public class Equation {
 
    private final Pebbles left;
    private final Pebbles right;
 
    // Pebbles Pebbles -> Equation
    // creates an equation with the given two sides
    // throws IllegalArgumentException if either side is empty, exceeds 4
    // pebbles, or the two sides share a color
    public Equation(Pebbles left, Pebbles right) {
        validateSide(left, "left");
        validateSide(right, "right");
        if (!left.isDisjointFrom(right)) {
            throw new IllegalArgumentException(
                "Both sides of an equation must not share any pebble color.");
        }
        this.left  = left;
        this.right = right;
    }
 
    // Equation -> Pebbles
    // returns the left side of this equation
    public Pebbles getLeft() {
        return left;
    }
 
    // Equation -> Pebbles
    // returns the right side of this equation
    public Pebbles getRight() {
        return right;
    }
 
    // Equation Pebbles Pebbles -> boolean
    // true if the player can apply this equation left-to-right
    // (give left, receive right) given their wallet and the bank
    public boolean canApplyLeftToRight(Pebbles wallet, Pebbles bank) {
        return wallet.hasAtLeast(left) && bank.hasAtLeast(right);
    }
 
    // Equation Pebbles Pebbles -> boolean
    // true if the player can apply this equation right-to-left
    // (give right, receive left) given their wallet and the bank
    public boolean canApplyRightToLeft(Pebbles wallet, Pebbles bank) {
        return wallet.hasAtLeast(right) && bank.hasAtLeast(left);
    }
 
    // Equation Pebbles Pebbles -> boolean
    // true if the equation can be applied in at least one direction
    public boolean canApply(Pebbles wallet, Pebbles bank) {
        return canApplyLeftToRight(wallet, bank)
            || canApplyRightToLeft(wallet, bank);
    }
 
    // Equation Object -> boolean
    // true if this and that equation represent the same trade
    // two equations are equal if their sides match in either orientation
    // since an equation is bidirectional
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equation)) return false;
        Equation that = (Equation) o;
        return (this.left.equals(that.left)  && this.right.equals(that.right))
            || (this.left.equals(that.right) && this.right.equals(that.left));
    }
 
    // Equation -> int
    // hash code consistent with equals: uses addition so both
    // orientations of the same equation produce the same hash
    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }
 
    // Equation -> String
    // text representation of this equation
    @Override
    public String toString() {
        return left.toString() + " = " + right.toString();
    }
 
    // String Pebbles -> void
    // validates that the given side has between 1 and 4 pebbles
    // throws IllegalArgumentException if not
    private static void validateSide(Pebbles side, String name) {
        if (side == null || side.isEmpty() || side.size() > 4) {
            throw new IllegalArgumentException(
                "The " + name + " side must have 1-4 pebbles.");
        }
    }
}