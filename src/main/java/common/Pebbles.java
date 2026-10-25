package common;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
 
/*
 * A multiset of pebbles: a collection where color matters but
 * order does not, and the same color can appear more than once.
 *
 * Used to represent a player's wallet, the bank's supply, and
 * each side of an equation.
 *
 * Data representation:
 *   counts: a map from Pebble to a positive integer count.
 *   Colors with zero count are absent from the map entirely.
 *
 * Invariant: all counts are strictly positive.
 */
public class Pebbles {
 
    private final Map<Pebble, Integer> counts;
 
    // -> Pebbles
    // creates an empty pebble collection
    public Pebbles() {
        this.counts = new EnumMap<>(Pebble.class);
    }
 
    // List<Pebble> -> Pebbles
    // creates a pebble collection from a flat list
    // colors may repeat 
    // order does not matter
    public Pebbles(List<Pebble> pebbles) {
        this.counts = new EnumMap<>(Pebble.class);
        for (Pebble p : pebbles) {
            counts.merge(p, 1, Integer::sum);
        }
    }
 
    // Map<Pebble, Integer> -> Pebbles
    // creates a pebble collection from an existing count map
    // colors with a count of zero are ignored
    public Pebbles(Map<Pebble, Integer> counts) {
        this.counts = new EnumMap<>(Pebble.class);
        for (Map.Entry<Pebble, Integer> e : counts.entrySet()) {
            if (e.getValue() > 0) {
                this.counts.put(e.getKey(), e.getValue());
            }
        }
    }
 
    // Pebbles Pebbles -> boolean
    // true if this collection has at least as many of each color as that collection
    // used to check if a player can afford a trade or card purchase
    public boolean hasAtLeast(Pebbles that) {
        for (Map.Entry<Pebble, Integer> e : that.counts.entrySet()) {
            if (this.counts.getOrDefault(e.getKey(), 0) < e.getValue()) {
                return false;
            }
        }
        return true;
    }
 
    // Pebbles Pebble -> int
    // returns how many pebbles of the given color are in this collection
    // returns 0 if the color is absent
    public int countOf(Pebble color) {
        return counts.getOrDefault(color, 0);
    }
 
    // Pebbles -> int
    // total number of pebbles across all colors
    public int size() {
        int total = 0;
        for (int n : counts.values()) total += n;
        return total;
    }
 
    // Pebbles -> boolean
    // true if this collection has no pebbles of any color
    public boolean isEmpty() {
        return counts.isEmpty();
    }
 
    // Pebbles Pebbles -> boolean
    // true if this collection and that collection share no colors
    // used to validate that equation sides are disjoint
    public boolean isDisjointFrom(Pebbles that) {
        for (Pebble p : that.counts.keySet()) {
            if (this.counts.containsKey(p)) return false;
        }
        return true;
    }
 
    // Pebbles Pebbles -> Pebbles
    // returns a new collection with all pebbles from both this collection and that collection
    // does NOT modify this  
    // used when a player receives pebbles from a trade
    public Pebbles add(Pebbles that) {
        Map<Pebble, Integer> result = new EnumMap<>(this.counts);
        for (Map.Entry<Pebble, Integer> e : that.counts.entrySet()) {
            result.merge(e.getKey(), e.getValue(), Integer::sum);
        }
        return new Pebbles(result);
    }
 
    // Pebbles Pebbles -> Pebbles
    // returns a new collection with that collection removed from this collection
    // throws IllegalArgumentException if this collection doesn't have enough of some color
    // does NOT modify this
    // used when a player pays for a trade or card
    public Pebbles remove(Pebbles that) {
        if (!this.hasAtLeast(that)) {
            throw new IllegalArgumentException(
                "Cannot remove pebbles not present in this collection.");
        }
        Map<Pebble, Integer> result = new EnumMap<>(this.counts);
        for (Map.Entry<Pebble, Integer> e : that.counts.entrySet()) {
            int remaining = result.get(e.getKey()) - e.getValue();
            if (remaining == 0) {
                result.remove(e.getKey());
            } else {
                result.put(e.getKey(), remaining);
            }
        }
        return new Pebbles(result);
    }
 
    // Pebbles -> List<Pebble>
    // returns all pebbles as a flat list in canonical order: R W B G Y
    // used for rendering and for JSON serialization in test harnesses
    public List<Pebble> toList() {
        List<Pebble> result = new ArrayList<>();
        for (Pebble p : Pebble.values()) {
            for (int i = 0; i < counts.getOrDefault(p, 0); i++) {
                result.add(p);
            }
        }
        return Collections.unmodifiableList(result);
    }
 
    // Pebbles Object -> boolean
    // true if this and 'that' have the same counts of each color
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pebbles)) return false;
        Pebbles that = (Pebbles) o;
        return this.counts.equals(that.counts);
    }
 
    // Pebbles -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        return counts.hashCode();
    }
 
    // Pebbles -> String
    // text representation listing each pebble abbreviation in R W B G Y order
    // example: two red and one blue -> "R R B"
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pebble p : Pebble.values()) {
            int n = counts.getOrDefault(p, 0);
            for (int i = 0; i < n; i++) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(p.abbreviation());
            }
        }
        return sb.toString();
    }
}