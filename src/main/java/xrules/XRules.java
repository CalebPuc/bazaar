package xrules;
 
import com.google.gson.*;
import common.*;
 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
 
/*
 * Integration test harness for the exchange legality check.
 *
 * Reads from STDIN:
 *   1. *Equations -- the table of equations
 *   2. *Rules    -- the proposed exchange sequence (ordered)
 *   3. *Turn     -- the turn state for the active player
 *
 * Writes to STDOUT:
 *   false                       -- if the exchanges are illegal
 *   *Pebbles then *Pebbles      -- if legal: updated wallet, then updated bank
 *
 * Uses RuleBook.isLegalExchangeRequest to check legality.
 */
public class XRules {
 
    public static void main(String[] args) {
        JsonStreamParser parser =
            new JsonStreamParser(new InputStreamReader(System.in));
 
        Equations      equations = parseEquations(parser.next());
        List<Pebbles[]> exchanges = parseExchanges(parser.next());
        TurnState       turn      = parseTurnState(parser.next());
 
        boolean legal = RuleBook.isLegalExchangeRequest(
            turn, exchanges, equations);
 
        Gson gson = new Gson();
 
        if (!legal) {
            System.out.println("false");
            return;
        }
 
        // apply the exchanges to get resulting wallet and bank
        Pebbles wallet = turn.getActive().getWallet();
        Pebbles bank   = turn.getBank();
        for (Pebbles[] step : exchanges) {
            wallet = wallet.remove(step[0]).add(step[1]);
            bank   = bank.remove(step[1]).add(step[0]);
        }
 
        System.out.println(gson.toJson(serializePebbles(wallet)));
        System.out.println(gson.toJson(serializePebbles(bank)));
    }
 
    // String -> Pebble
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
    static Pebbles parsePebbles(JsonElement json) {
        List<Pebble> list = new ArrayList<>();
        for (JsonElement p : json.getAsJsonArray()) {
            list.add(parsePebble(p.getAsString()));
        }
        return new Pebbles(list);
    }
 
    // JsonElement -> Equation
    static Equation parseEquation(JsonElement json) {
        JsonArray arr = json.getAsJsonArray();
        return new Equation(parsePebbles(arr.get(0)), parsePebbles(arr.get(1)));
    }
 
    // JsonElement -> Equations
    static Equations parseEquations(JsonElement json) {
        List<Equation> result = new ArrayList<>();
        for (JsonElement eq : json.getAsJsonArray()) {
            result.add(parseEquation(eq));
        }
        return new Equations(result);
    }
 
    // JsonElement -> List<Pebbles[]>
    // parses a *Rules array into a list of [given, received] pairs
    // each rule is a two-element equation array -- the order is
    // the direction the player intends to apply it
    static List<Pebbles[]> parseExchanges(JsonElement json) {
        List<Pebbles[]> result = new ArrayList<>();
        for (JsonElement step : json.getAsJsonArray()) {
            JsonArray arr = step.getAsJsonArray();
            Pebbles given    = parsePebbles(arr.get(0));
            Pebbles received = parsePebbles(arr.get(1));
            result.add(new Pebbles[]{given, received});
        }
        return result;
    }
 
    // JsonElement -> Card
    static Card parseCard(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new Card(parsePebbles(obj.get("pebbles")),
                        obj.get("face?").getAsBoolean());
    }
 
    // JsonElement -> Cards
    static Cards parseCards(JsonElement json) {
        List<Card> list = new ArrayList<>();
        for (JsonElement c : json.getAsJsonArray()) {
            list.add(parseCard(c));
        }
        return new Cards(list);
    }
 
    // JsonElement -> PlayerState
    static PlayerState parsePlayer(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new PlayerState(parsePebbles(obj.get("wallet")),
                               obj.get("score").getAsInt());
    }
 
    // JsonElement -> TurnState
    static TurnState parseTurnState(JsonElement json) {
        JsonObject obj    = json.getAsJsonObject();
        Pebbles bank      = parsePebbles(obj.get("bank"));
        Cards cards       = parseCards(obj.get("cards"));
        PlayerState active = parsePlayer(obj.get("active"));
        List<Integer> scores = new ArrayList<>();
        for (JsonElement s : obj.get("scores").getAsJsonArray()) {
            scores.add(s.getAsInt());
        }
        return new TurnState(bank, cards, active, scores);
    }
 
    // Pebbles -> JsonArray
    static JsonArray serializePebbles(Pebbles pebbles) {
        JsonArray arr = new JsonArray();
        for (Pebble p : pebbles.toList()) {
            arr.add(p.name().toLowerCase());
        }
        return arr;
    }
}