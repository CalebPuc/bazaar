package xgames;
 
import com.google.gson.*;
import common.*;
import player.*;
import referee.*;
 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/*
 * Integration test harness for a complete game run.
 *
 * Reads from STDIN:
 *   1. *Actors    -- the players, with names, policies, and optional
 *                    exception-raising method
 *   2. *Equations -- the equation table for this game
 *   3. *Game      -- the game state to start from
 *
 * Writes to STDOUT:
 *   two JSON arrays:
 *   1. alphabetically sorted winner names
 *   2. alphabetically sorted dropout names
 *
 * An Actor is either:
 *   [ActorName, Policy]         -- well-behaved player
 *   [ActorName, Policy, Exn]    -- throws when the named method is called
 *
 * Exn is one of: "setup", "request-pebble-or-trades", "request-cards", "win"
 */
public class XGames {
 
    public static void main(String[] args) {
        JsonStreamParser parser =
            new JsonStreamParser(new InputStreamReader(System.in));
 
        List<Mechanism> mechanisms = parseActors(parser.next());
        Equations       equations  = parseEquations(parser.next());
        GameState       state      = parseGame(parser.next());
 
        Referee ref = new Referee(equations);
        GameResult result = ref.runGame(mechanisms, state);
 
        List<String> winners    = new ArrayList<>(result.getWinners());
        List<String> misbehaved = new ArrayList<>(result.getMisbehaved());
        Collections.sort(winners);
        Collections.sort(misbehaved);
 
        Gson gson = new Gson();
        System.out.println(gson.toJson(winners));
        System.out.println(gson.toJson(misbehaved));
    }
 
    // JsonElement -> List<Mechanism>
    // parses an *Actors array into a list of mechanisms
    // two-element actors are well-behaved
    // three-element actors throw when the named method is called
    static List<Mechanism> parseActors(JsonElement json) {
        List<Mechanism> result = new ArrayList<>();
        for (JsonElement actorElem : json.getAsJsonArray()) {
            JsonArray actor  = actorElem.getAsJsonArray();
            String    name   = actor.get(0).getAsString();
            String    policy = actor.get(1).getAsString();
            Strategy  strat  = makeStrategy(policy);
 
            if (actor.size() == 2) {
                result.add(new Mechanism(name, strat));
            } else {
                String exn = actor.get(2).getAsString();
                result.add(makeExnMechanism(name, strat, exn));
            }
        }
        return result;
    }
 
    // String -> Strategy
    // creates a strategy from a policy string
    static Strategy makeStrategy(String policy) {
        if (policy.equals("purchase-points")) {
            return new PurchasePointsStrategy();
        } else {
            return new PurchaseSizeStrategy();
        }
    }
 
    // String Strategy String -> Mechanism
    // creates a mechanism that throws when the named method is called
    static Mechanism makeExnMechanism(String name, Strategy strat,
                                       String exn) {
        return new Mechanism(name, strat) {
            @Override
            public void setup(Equations equations) {
                if (exn.equals("setup")) {
                    throw new RuntimeException(name + " throws on setup");
                }
                super.setup(equations);
            }
 
            @Override
            public boolean wantsPebble(TurnState turn, Equations equations) {
                if (exn.equals("request-pebble-or-trades")) {
                    throw new RuntimeException(
                        name + " throws on request-pebble-or-trades");
                }
                return super.wantsPebble(turn, equations);
            }
 
            @Override
            public List<Pebbles[]> requestExchanges(TurnState turn,
                                                     Equations equations) {
                if (exn.equals("request-pebble-or-trades")) {
                    throw new RuntimeException(
                        name + " throws on request-pebble-or-trades");
                }
                return super.requestExchanges(turn, equations);
            }
 
            @Override
            public List<Card> requestPurchases(TurnState turn,
                                               Equations equations) {
                if (exn.equals("request-cards")) {
                    throw new RuntimeException(
                        name + " throws on request-cards");
                }
                return super.requestPurchases(turn, equations);
            }
 
            @Override
            public void win(boolean won) {
                if (exn.equals("win")) {
                    throw new RuntimeException(name + " throws on win");
                }
                super.win(won);
            }
        };
    }
 
    // JsonElement -> Equations
    static Equations parseEquations(JsonElement json) {
        List<Equation> result = new ArrayList<>();
        for (JsonElement eq : json.getAsJsonArray()) {
            result.add(parseEquation(eq));
        }
        return new Equations(result);
    }
 
    // JsonElement -> Equation
    static Equation parseEquation(JsonElement json) {
        JsonArray arr = json.getAsJsonArray();
        return new Equation(parsePebbles(arr.get(0)),
                            parsePebbles(arr.get(1)));
    }
 
    // JsonElement -> GameState
    // parses a *Game object with bank, visibles, cards, players fields
    static GameState parseGame(JsonElement json) {
        JsonObject obj      = json.getAsJsonObject();
        Pebbles    bank     = parsePebbles(obj.get("bank"));
        Cards      visibles = parseCards(obj.get("visibles"));
        Cards      deck     = parseCards(obj.get("cards"));
        List<PlayerState> players = new ArrayList<>();
        for (JsonElement p : obj.get("players").getAsJsonArray()) {
            players.add(parsePlayer(p));
        }
        return new GameState(bank, visibles, deck, players);
    }
 
    // JsonElement -> PlayerState
    static PlayerState parsePlayer(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new PlayerState(parsePebbles(obj.get("wallet")),
                               obj.get("score").getAsInt());
    }
 
    // JsonElement -> Cards
    static Cards parseCards(JsonElement json) {
        List<Card> list = new ArrayList<>();
        for (JsonElement c : json.getAsJsonArray()) {
            list.add(parseCard(c));
        }
        return new Cards(list);
    }
 
    // JsonElement -> Card
    static Card parseCard(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new Card(parsePebbles(obj.get("pebbles")),
                        obj.get("face?").getAsBoolean());
    }
 
    // JsonElement -> Pebbles
    static Pebbles parsePebbles(JsonElement json) {
        List<Pebble> list = new ArrayList<>();
        for (JsonElement p : json.getAsJsonArray()) {
            list.add(parsePebble(p.getAsString()));
        }
        return new Pebbles(list);
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
}