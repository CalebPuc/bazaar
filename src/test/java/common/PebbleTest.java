package common;
 
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
 
/*
 * Unit tests for the Pebble enum.
 *
 * Tests are organized by method. Each test covers one specific
 * behavior described in the purpose statement of the method.
 */
public class PebbleTest {
 
    @Test
    void abbreviationReturnsFirstLetterOfName() {
        assertEquals("R", Pebble.RED.abbreviation());
        assertEquals("W", Pebble.WHITE.abbreviation());
        assertEquals("B", Pebble.BLUE.abbreviation());
        assertEquals("G", Pebble.GREEN.abbreviation());
        assertEquals("Y", Pebble.YELLOW.abbreviation());
    }
 
    @Test
    void hexColorReturnsNonNullStringForAllColors() {
        for (Pebble p : Pebble.values()) {
            assertNotNull(p.hexColor());
        }
    }
 
    @Test
    void hexColorStartsWithHashForAllColors() {
        for (Pebble p : Pebble.values()) {
            assertTrue(p.hexColor().startsWith("#"));
        }
    }
 
    @Test
    void fiveDistinctValuesExist() {
        assertEquals(5, Pebble.values().length);
    }
}