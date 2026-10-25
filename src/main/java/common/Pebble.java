package common;
 
/*
 * One of the five pebble colors in the Bazaar game.
 *
 * Pebbles are the basic unit of currency: players hold them
 * in wallets, the bank holds a supply, and equations specify
 * trades in terms of them.
 *
 * Data representation:
 *   An enum with exactly five values: RED, WHITE, BLUE, GREEN, YELLOW.
 */
public enum Pebble {
    RED, WHITE, BLUE, GREEN, YELLOW;
 
    // Pebble -> String
    // returns the single-character abbreviation for this color
    // used when rendering equations and cards as text (R, W, B, G, Y)
    public String abbreviation() {
        return this.name().substring(0, 1);
    }
 
    // Pebble -> String
    // returns the hex color string for graphical rendering
    public String hexColor() {
        switch (this) {
            case RED:    return "#E74C3C";
            case WHITE:  return "#ECF0F1";
            case BLUE:   return "#3498DB";
            case GREEN:  return "#2ECC71";
            case YELLOW: return "#F1C40F";
            default:     throw new IllegalStateException("Unknown pebble: " + this);
        }
    }
}