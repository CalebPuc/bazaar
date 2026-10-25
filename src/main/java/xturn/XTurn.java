package xturn;
 
import com.google.gson.*;
import common.*;
import referee.GameState;
 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
 
/*
 * Integration test harness for the GameState -> TurnState transformation.
 *
 * Reads from STDIN:
 *   *Game -- the referee's full game state
 *
 * Writes to STDOUT:
 *   *Turn -- the turn state for the active player
 *
 * The active player is the first player in the "players" array.
 * The "scores" output contains only the remaining players' scores.
 */
public class XTurn {
 
    public static void main(String[] args) {
        JsonStreamParser parser =
            new JsonStreamParser(new InputStreamReader(System.in));
 
        GameState game = parseGame(parser.next());
        TurnState turn = game.toTurnState();
 
        System.out.println(new Gson().toJson(serializeTurnState(turn)));
    }
 
    // JsonElement -> Pebble
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
 
    // JsonElement -> Card
    // parses a JSON object with "pebbles" and "face?" fields
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
    // parses a JSON object with "wallet" and "score" fields
    static PlayerState parsePlayer(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new PlayerState(parsePebbles(obj.get("wallet")),
                               obj.get("score").getAsInt());
    }
 
    // JsonElement -> GameState
    // parses a JSON object with "bank", "visibles", "cards", "players" fields
    static GameState parseGame(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        Pebbles bank     = parsePebbles(obj.get("bank"));
        Cards visibles   = parseCards(obj.get("visibles"));
        Cards deck       = parseCards(obj.get("cards"));
        List<PlayerState> players = new ArrayList<>();
        for (JsonElement p : obj.get("players").getAsJsonArray()) {
            players.add(parsePlayer(p));
        }
        return new GameState(bank, visibles, deck, players);
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
 
    // Cards -> JsonArray
    static JsonArray serializeCards(Cards cards) {
        JsonArray arr = new JsonArray();
        for (Card c : cards.getCards()) {
            arr.add(serializeCard(c));
        }
        return arr;
    }
 
    // PlayerState -> JsonObject
    static JsonObject serializePlayer(PlayerState player) {
        JsonObject obj = new JsonObject();
        obj.add("wallet", serializePebbles(player.getWallet()));
        obj.addProperty("score", player.getScore());
        return obj;
    }
 
    // TurnState -> JsonObject
    // serializes a TurnState to the *Turn JSON format
    static JsonObject serializeTurnState(TurnState turn) {
        JsonObject obj = new JsonObject();
        obj.add("bank",   serializePebbles(turn.getBank()));
        obj.add("cards",  serializeCards(turn.getVisibles()));
        obj.add("active", serializePlayer(turn.getActive()));
        JsonArray scores = new JsonArray();
        for (int s : turn.getScores()) scores.add(s);
        obj.add("scores", scores);
        return obj;
    }
}