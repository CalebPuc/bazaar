package common;
 
/*
 * A single card in the Bazaar game.
 *
 * A card displays five pebbles and optionally a star. Players
 * purchase cards to earn points; the points depend on how many
 * pebbles the player has left after the purchase.
 *
 * Data representation:
 *   pebbles: a Pebbles with exactly 5 total pebbles
 *   hasStar: whether this card displays a star
 *
 * Invariant: pebbles contains exactly 5 pebbles.
 */
public class Card {
 
    private final Pebbles pebbles;
    private final boolean hasStar;
 
    // Pebbles boolean -> Card
    // creates a card with the given pebbles and star status
    // throws IllegalArgumentException if pebbles does not have exactly 5
    public Card(Pebbles pebbles, boolean hasStar) {
        if (pebbles == null || pebbles.size() != 5) {
            throw new IllegalArgumentException(
                "A card must display exactly 5 pebbles.");
        }
        this.pebbles = pebbles;
        this.hasStar = hasStar;
    }
 
    // Card Pebbles -> boolean
    // true if the given wallet has at least the pebbles on this card
    // used to check if a player can afford to purchase this card
    public boolean canAcquire(Pebbles wallet) {
        return wallet.hasAtLeast(pebbles);
    }
 
    // Card -> Pebbles
    // returns the pebbles displayed on this card
    public Pebbles getPebbles() {
        return pebbles;
    }
 
    // Card -> boolean
    // true if this card displays a star
    public boolean hasStar() {
        return hasStar;
    }
 
    // Card Object -> boolean
    // true if this card and that card have the same pebbles and star status
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card that = (Card) o;
        return this.hasStar == that.hasStar
            && this.pebbles.equals(that.pebbles);
    }
 
    // Card -> int
    // hash code consistent with equals
    @Override
    public int hashCode() {
        return 31 * pebbles.hashCode() + (hasStar ? 1 : 0);
    }
 
    // Card -> String
    // text representation showing this card's pebbles and star status
    // example: "[ R W B G Y ]  *" for a starred card
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Pebble p : pebbles.toList()) {
            sb.append(" ").append(p.abbreviation());
        }
        sb.append(" ]");
        if (hasStar) {
            sb.append("  *");
        }
        return sb.toString();
    }
}