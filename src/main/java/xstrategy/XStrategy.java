package xstrategy;
 
import com.google.gson.*;
import common.*;
import player.*;
 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
 
/*
 * Integration test harness for the Strategy functionality.
 *
 * Reads from STDIN:
 *   1. *Equations -- the table of equations
 *   2. *Turn     -- the turn state for the active player
 *   3. *Policy   -- "purchase-points" or "purchase-size"
 *
 * Writes to STDOUT (four separate values):
 *   1. *Rules   -- the chosen exchanges in order
 *   2. *Cards   -- the chosen purchases in order
 *   3. Natural  -- the total points earned
 *   4. *Pebbles -- the player's wallet after all moves
 *
 * The input is guaranteed to allow at least one exchange.
 */
public class XStrategy {
 
    public static void main(String[] args) {
        JsonStreamParser parser =
            new JsonStreamParser(new InputStreamReader(System.in));
 
        Equations equations = parseEquations(parser.next());
        TurnState turn      = parseTurnState(parser.next());
        String    policy    = parser.next().getAsString();
 
        Strategy strategy = policy.equals("purchase-points")
            ? new PurchasePointsStrategy()
            : new PurchaseSizeStrategy();
 
        TurnDecision decision = strategy.takeTurn(turn, equations);
 
        Gson gson = new Gson();
        System.out.println(gson.toJson(serializeExchanges(decision.getExchanges())));
        System.out.println(gson.toJson(serializeCards(decision.getPurchases())));
        System.out.println(decision.getPoints());
        System.out.println(gson.toJson(serializePebbles(decision.getWallet())));
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
    // parses a *Turn JSON object into a TurnState
    static TurnState parseTurnState(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        Pebbles bank   = parsePebbles(obj.get("bank"));
        Cards cards    = parseCards(obj.get("cards"));
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
 
    // Card -> JsonObject
    static JsonObject serializeCard(Card card) {
        JsonObject obj = new JsonObject();
        obj.add("pebbles", serializePebbles(card.getPebbles()));
        obj.addProperty("face?", card.hasStar());
        return obj;
    }
 
    // List<Card> -> JsonArray
    static JsonArray serializeCards(List<Card> cards) {
        JsonArray arr = new JsonArray();
        for (Card c : cards) arr.add(serializeCard(c));
        return arr;
    }
 
    // Equation -> JsonArray
    // serializes one equation as [given, received] in the direction applied
    static JsonArray serializeEquation(Equation eq) {
        JsonArray arr = new JsonArray();
        arr.add(serializePebbles(eq.getLeft()));
        arr.add(serializePebbles(eq.getRight()));
        return arr;
    }
 
    // List<ExchangeStep> -> JsonArray
    // serializes exchanges in order, preserving the direction applied
    static JsonArray serializeExchanges(List<ExchangeStep> exchanges) {
        JsonArray result = new JsonArray();
        for (ExchangeStep step : exchanges) {
            JsonArray arr = new JsonArray();
            arr.add(serializePebbles(step.getGiven()));
            arr.add(serializePebbles(step.getReceived()));
            result.add(arr);
        }
        return result;
    }
}