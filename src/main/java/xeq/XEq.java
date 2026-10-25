package xeq;
 
import com.google.gson.*;
import common.*;
 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
 
/*
 * Integration test harness for equation filtering.
 *
 * Reads from STDIN:
 *   1. *Equations -- the table of equations
 *   2. *Wallet   -- the active player's pebbles
 *   3. *Bank     -- the bank's pebbles
 *
 * Writes to STDOUT:
 *   *Rules -- equations the player can apply in at least one direction
 */
public class XEq {
 
    public static void main(String[] args) {
        JsonStreamParser parser =
            new JsonStreamParser(new InputStreamReader(System.in));
 
        Equations equations = parseEquations(parser.next());
        Pebbles   wallet    = parsePebbles(parser.next());
        Pebbles   bank      = parsePebbles(parser.next());
 
        Equations applicable = equations.filterApplicable(wallet, bank);
 
        System.out.println(new Gson().toJson(serializeEquations(applicable)));
    }
 
    // JsonElement -> Pebble
    // parses a lowercase color string into a Pebble
    static Pebble parsePebble(String s) {
        switch (s) {
            case "red":    return Pebble.RED;
            case "white":  return Pebble.WHITE;
            case "blue":   return Pebble.BLUE;
            case "green":  return Pebble.GREEN;
            case "yellow": return Pebble.YELLOW;
            default: throw new IllegalArgumentException("Unknown pebble: " + s);
        }
    }
 
    // JsonElement -> Pebbles
    // parses a JSON array of pebble color strings
    static Pebbles parsePebbles(JsonElement json) {
        List<Pebble> list = new ArrayList<>();
        for (JsonElement p : json.getAsJsonArray()) {
            list.add(parsePebble(p.getAsString()));
        }
        return new Pebbles(list);
    }
 
    // JsonElement -> Equation
    // parses a two-element JSON array into an Equation
    static Equation parseEquation(JsonElement json) {
        JsonArray arr = json.getAsJsonArray();
        return new Equation(parsePebbles(arr.get(0)), parsePebbles(arr.get(1)));
    }
 
    // JsonElement -> Equations
    // parses a JSON array of equation arrays
    static Equations parseEquations(JsonElement json) {
        List<Equation> result = new ArrayList<>();
        for (JsonElement eq : json.getAsJsonArray()) {
            result.add(parseEquation(eq));
        }
        return new Equations(result);
    }
 
    // Pebbles -> JsonArray
    // serializes a pebble collection as an array of color strings
    static JsonArray serializePebbles(Pebbles pebbles) {
        JsonArray arr = new JsonArray();
        for (Pebble p : pebbles.toList()) {
            arr.add(p.name().toLowerCase());
        }
        return arr;
    }
 
    // Equation -> JsonArray
    // serializes one equation as a two-element array
    static JsonArray serializeEquation(Equation eq) {
        JsonArray arr = new JsonArray();
        arr.add(serializePebbles(eq.getLeft()));
        arr.add(serializePebbles(eq.getRight()));
        return arr;
    }
 
    // Equations -> JsonArray
    // serializes an equation table as an array of equation arrays
    static JsonArray serializeEquations(Equations equations) {
        JsonArray result = new JsonArray();
        for (Equation eq : equations.getEquations()) {
            result.add(serializeEquation(eq));
        }
        return result;
    }
}