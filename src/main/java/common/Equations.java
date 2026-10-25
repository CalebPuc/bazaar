package common;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
 
/*
 * The table of equations in the Bazaar game: a collection of up
 * to 10 equations fixed at the start of a game and visible to all.
 *
 * Both the referee and players consult this to determine which
 * trades are possible and which exchange requests are legal.
 *
 * Data representation:
 *   A list of up to 10 Equation values, treated as an unordered set.
 *
 * Invariant: 0-10 equations, no duplicates.
 */
public class Equations {
 
    private final List<Equation> equations;
 
    // List<Equation> -> Equations
    // creates a table from the given list
    // throws IllegalArgumentException if the list has more than 10 equations
    public Equations(List<Equation> equations) {
        if (equations == null || equations.size() > 10) {
            throw new IllegalArgumentException(
                "An equation table must contain 0-10 equations.");
        }
        this.equations = Collections.unmodifiableList(new ArrayList<>(equations));
    }
 
    // -> Equations
    // returns a new table of exactly 10 randomly generated equations
    // used by the referee at game setup
    public static Equations createRandom() {
        Random rng = new Random();
        List<Equation> result = new ArrayList<>();
        while (result.size() < 10) {
            try {
                result.add(randomEquation(rng));
            } catch (IllegalArgumentException ignored) {
                // retry if random generation violates a constraint
            }
        }
        return new Equations(result);
    }
 
    // Equations Pebbles Pebbles -> Equations
    // returns a new table with only the equations the player can apply
    // in at least one direction given their wallet and the bank
    // used by a player to find applicable trades on its turn
    public Equations filterApplicable(Pebbles wallet, Pebbles bank) {
        List<Equation> applicable = new ArrayList<>();
        for (Equation eq : equations) {
            if (eq.canApply(wallet, bank)) {
                applicable.add(eq);
            }
        }
        return new Equations(applicable);
    }
 
    // Equations -> List<Equation>
    // returns a read-only view of the equations in this table
    public List<Equation> getEquations() {
        return equations;
    }
 
    // Equations -> int
    // number of equations in this table
    public int size() {
        return equations.size();
    }
 
    // Equations -> boolean
    // true if this table has no equations
    public boolean isEmpty() {
        return equations.isEmpty();
    }
 
    // Equations Object -> boolean
    // true if this table and that table contain the same equations in the same order
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equations)) return false;
        Equations that = (Equations) o;
        return this.equations.equals(that.equations);
    }
 
    // Equations -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        return equations.hashCode();
    }
 
    // Equations -> String
    // text representation listing each equation numbered from 1
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Equations:\n");
        for (int i = 0; i < equations.size(); i++) {
            sb.append(String.format("  %2d.  %s%n", i + 1, equations.get(i).toString()));
        }
        return sb.toString();
    }
 
    // Random -> Equation
    // generates one random equation by shuffling colors and splitting them
    private static Equation randomEquation(Random rng) {
        List<Pebble> colors = new ArrayList<>(List.of(Pebble.values()));
        Collections.shuffle(colors, rng);
        int split = 1 + rng.nextInt(colors.size() - 1);
        Pebbles left  = randomSide(rng, colors.subList(0, split));
        Pebbles right = randomSide(rng, colors.subList(split, colors.size()));
        return new Equation(left, right);
    }
 
    // Random List<Pebble> -> Pebbles
    // samples 1-4 pebbles from the given color set to form one side
    private static Pebbles randomSide(Random rng, List<Pebble> available) {
        int count = 1 + rng.nextInt(4);
        List<Pebble> side = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            side.add(available.get(rng.nextInt(available.size())));
        }
        return new Pebbles(side);
    }
}