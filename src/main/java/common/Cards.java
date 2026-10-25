package common;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
 
/*
 * An ordered collection of cards in the Bazaar game.
 *
 * Used for both the face-down draw deck and the visible cards
 * on the table. Order matters: the first card is the next
 * one to be drawn or the first visible card.
 *
 * Data representation:
 *   A list of Card values. May contain duplicates.
 *
 * Invariant: the list is non-null.
 */
public class Cards {
 
    private final List<Card> cards;
 
    // List<Card> -> Cards
    // creates a Cards collection from the given list
    public Cards(List<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Card list must not be null.");
        }
        this.cards = Collections.unmodifiableList(new ArrayList<>(cards));
    }
 
    // -> Cards
    // returns a new collection of 20 randomly generated cards
    // used by the referee at game setup
    public static Cards createRandom() {
        Random rng = new Random();
        Pebble[] colors = Pebble.values();
        List<Card> result = new ArrayList<>();
        while (result.size() < 20) {
            List<Pebble> pebbleList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                pebbleList.add(colors[rng.nextInt(colors.length)]);
            }
            result.add(new Card(new Pebbles(pebbleList), rng.nextBoolean()));
        }
        return new Cards(result);
    }
 
    // Cards Pebbles -> boolean
    // true if the given wallet can afford at least one card in this collection
    // used by the referee to check if a player can buy anything on its turn
    public boolean canAcquireAny(Pebbles wallet) {
        for (Card card : cards) {
            if (card.canAcquire(wallet)) {
                return true;
            }
        }
        return false;
    }
 
    // Cards -> List<Card>
    // returns a read-only view of the cards in this collection
    public List<Card> getCards() {
        return cards;
    }
 
    // Cards -> int
    // number of cards in this collection
    public int size() {
        return cards.size();
    }
 
    // Cards -> boolean
    // true if this collection has no cards
    public boolean isEmpty() {
        return cards.isEmpty();
    }
 
    // Cards -> Card
    // returns the first card in this collection
    // throws IllegalStateException if this collection is empty
    public Card getFirst() {
        if (cards.isEmpty()) {
            throw new IllegalStateException(
                "Cannot get first card of an empty collection.");
        }
        return cards.get(0);
    }
 
    // Cards -> Cards
    // returns a new Cards with the first card removed
    // does NOT modify this
    // used when the referee draws a card
    // throws IllegalStateException if this collection is empty
    public Cards removeFirst() {
        if (cards.isEmpty()) {
            throw new IllegalStateException(
                "Cannot remove from an empty collection.");
        }
        return new Cards(cards.subList(1, cards.size()));
    }
 
    // Cards Object -> boolean
    // true if this collection and that collection contain the same cards in the same order
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cards)) return false;
        Cards that = (Cards) o;
        return this.cards.equals(that.cards);
    }
 
    // Cards -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        return cards.hashCode();
    }
 
    // Cards -> String
    // text representation listing each card numbered from 1
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cards:\n");
        for (int i = 0; i < cards.size(); i++) {
            sb.append(String.format("  %2d.  %s%n", i + 1, cards.get(i).toString()));
        }
        return sb.toString();
    }
}