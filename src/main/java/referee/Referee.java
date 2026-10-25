package referee;
 
import common.*;
import player.Mechanism;
 
import java.util.ArrayList;
import java.util.List;
 
/*
 * The referee for the Bazaar game.
 *
 * Runs a complete game by granting players turns one at a time
 * until a terminal state is reached. Eliminates any player that
 * misbehaves: illegal response, exception, or null return.
 *
 * The active player is always index 0 in both the GameState player
 * list and the parallel mechanisms list. They are kept in sync:
 * whenever a player is removed from one, it is removed from the other.
 *
 * Misbehavior handled here (logical level):
 *   - illegal exchange request
 *   - illegal pebble request
 *   - illegal card purchase
 *   - any exception thrown by the mechanism
 */
public class Referee {
 
    private final Equations equations;
 
    // Equations -> Referee
    // creates a referee for a game with the given equation table
    public Referee(Equations equations) {
        this.equations = equations;
    }
 
    // Referee List<Mechanism> -> GameResult
    // runs a complete game with the given players starting from a
    // freshly generated initial state
    public GameResult runGame(List<Mechanism> players) {
        List<PlayerState> playerStates = new ArrayList<>();
        for (Mechanism m : players) {
            playerStates.add(new PlayerState(new Pebbles(), 0));
        }
        List<Pebble> allPebbles = new ArrayList<>();
        for (Pebble p : Pebble.values()) {
            for (int i = 0; i < 20; i++) allPebbles.add(p);
        }
        Pebbles bank   = new Pebbles(allPebbles);
        Cards allCards = Cards.createRandom();
        Cards visibles = new Cards(allCards.getCards().subList(0, 4));
        Cards deck     = new Cards(allCards.getCards().subList(4, 20));
        GameState initial = new GameState(bank, visibles, deck, playerStates);
        return runGame(players, initial);
    }
 
    // Referee List<Mechanism> GameState -> GameResult
    // runs a game to completion starting from the given state
    // mechanisms must be in the same order as the players in state
    public GameResult runGame(List<Mechanism> players, GameState initial) {
        List<Mechanism> mechs      = new ArrayList<>(players);
        List<String>    misbehaved = new ArrayList<>();
        GameState       state      = initial;

        // setup -- inform each player of the equations before play begins
        // players that throw during setup are eliminated immediately
        // runs in turn order so misbehaved players are recorded in the
        // order they were eliminated
        for (int i = 0; i < mechs.size(); ) {
            try {
                mechs.get(i).setup(equations);
                i++;
            } catch (Exception e) {
                misbehaved.add(mechs.get(i).name());
                List<PlayerState> remaining = new ArrayList<>(state.getPlayers());
                remaining.remove(i);
                state = new GameState(state.getBank(), state.getVisibles(),
                    state.getDeck(), remaining);
                mechs.remove(i);
            }
        }
 
        // stalemate detection: if nothing changes for a full round, stop
        int stalemateTurns    = 0;
        int maxStalemateTurns = mechs.size() * 2 + 1;
 
        while (!state.isGameOver() && !mechs.isEmpty()) {
 
            if (stalemateTurns > maxStalemateTurns) {
                break;
            }
 
            Mechanism active   = mechs.get(0);
            TurnState turn     = state.toTurnState();
            GameState before   = state;
            boolean eliminated = false;
 
            // first action: exchange or pebble request
            try {
                if (active.wantsPebble(turn, equations)) {
                    if (!RuleBook.isLegalPebbleRequest(turn, equations)) {
                        misbehaved.add(active.name());
                        state = state.withActivePlayerRemoved();
                        mechs.remove(0);
                        eliminated = true;
                    } else {
                        state = applyPebbleDraw(state);
                    }
                } else {
                    List<Pebbles[]> exchanges =
                        active.requestExchanges(turn, equations);
                    if (!RuleBook.isLegalExchangeRequest(
                            turn, exchanges, equations)) {
                        misbehaved.add(active.name());
                        state = state.withActivePlayerRemoved();
                        mechs.remove(0);
                        eliminated = true;
                    } else {
                        state = applyExchanges(state, exchanges);
                    }
                }
            } catch (Exception e) {
                misbehaved.add(active.name());
                state = state.withActivePlayerRemoved();
                mechs.remove(0);
                eliminated = true;
            }
 
            if (eliminated || state.isGameOver() || mechs.isEmpty()) {
                stalemateTurns = 0;
                continue;
            }
 
            // second action: card purchases
            // checked against the state after the first action, so the
            // wallet already includes any exchanges or drawn pebble;
            // no exchanges are passed, which avoids applying them twice
            try {
                TurnState afterFirst = state.toTurnState();
                List<Card> purchases = active.requestPurchases(
                    afterFirst, equations);
                if (!RuleBook.isLegalPurchaseRequest(
                        afterFirst, purchases, List.of())) {
                    misbehaved.add(active.name());
                    state = state.withActivePlayerRemoved();
                    mechs.remove(0);
                    stalemateTurns = 0;
                } else {
                    state = applyPurchases(state, purchases);
                    state = state.withRotatedPlayers();
                    mechs.add(mechs.remove(0));
 
                    if (state.equals(before.withRotatedPlayers())) {
                        stalemateTurns++;
                    } else {
                        stalemateTurns = 0;
                    }
                }
            } catch (Exception e) {
                misbehaved.add(active.name());
                state = state.withActivePlayerRemoved();
                mechs.remove(0);
                stalemateTurns = 0;
            }
        }
 
        // determine winners from remaining players
        List<String> winners = determineWinners(state, mechs);
 
        // notify each surviving player of the outcome
        // players that throw during win move from winners to dropouts
        List<String> finalWinners = new ArrayList<>(winners);
        for (int i = 0; i < mechs.size(); i++) {
            boolean won = winners.contains(mechs.get(i).name());
            try {
                mechs.get(i).win(won);
            } catch (Exception e) {
                String name = mechs.get(i).name();
                finalWinners.remove(name);
                misbehaved.add(name);
            }
        }
 
        return new GameResult(finalWinners, misbehaved);
    }
 
    // Referee GameState -> GameState
    // picks a pebble from the bank and adds it to the active player's wallet
    private GameState applyPebbleDraw(GameState state) {
        Pebble  picked    = RuleBook.pickPebble(state.getBank());
        Pebbles onePebble = new Pebbles(List.of(picked));
        return state
            .withBank(state.getBank().remove(onePebble))
            .withUpdatedActivePlayer(
                state.getActivePlayer().withWallet(
                    state.getActivePlayer().getWallet().add(onePebble)));
    }
 
    // Referee GameState List<Pebbles[]> -> GameState
    // applies a legal exchange sequence: updates wallet and bank
    private GameState applyExchanges(GameState state,
                                      List<Pebbles[]> exchanges) {
        Pebbles wallet = state.getActivePlayer().getWallet();
        Pebbles bank   = state.getBank();
        for (Pebbles[] step : exchanges) {
            wallet = wallet.remove(step[0]).add(step[1]);
            bank   = bank.remove(step[1]).add(step[0]);
        }
        return state
            .withBank(bank)
            .withUpdatedActivePlayer(
                state.getActivePlayer().withWallet(wallet));
    }
 
    // Referee GameState List<Card> -> GameState
    // applies a legal card purchase sequence: removes cards, awards points,
    // replaces from deck if possible
    private GameState applyPurchases(GameState state, List<Card> purchases) {
        if (purchases.isEmpty()) return state;
 
        Pebbles wallet   = state.getActivePlayer().getWallet();
        Cards   visibles = state.getVisibles();
        Cards   deck     = state.getDeck();
        int     points   = 0;
 
        for (Card card : purchases) {
            wallet  = wallet.remove(card.getPebbles());
            points += RuleBook.score(card, wallet.size());
            visibles = removeCard(visibles, card);
            if (!deck.isEmpty()) {
                visibles = addCard(visibles, deck.getFirst());
                deck     = deck.removeFirst();
            }
        }
 
        return state
            .withUpdatedActivePlayer(
                state.getActivePlayer()
                    .withWallet(wallet)
                    .withAddedScore(points))
            .withUpdatedCards(visibles, deck);
    }
 
    // GameState List<Mechanism> -> List<String>
    // returns the names of remaining players with the highest score
    private List<String> determineWinners(GameState state,
                                           List<Mechanism> mechs) {
        if (state.getPlayers().isEmpty() || mechs.isEmpty()) {
            return new ArrayList<>();
        }
        int maxScore = 0;
        for (PlayerState p : state.getPlayers()) {
            if (p.getScore() > maxScore) maxScore = p.getScore();
        }
        List<String> winners = new ArrayList<>();
        for (int i = 0; i < state.getPlayers().size(); i++) {
            if (state.getPlayers().get(i).getScore() == maxScore) {
                winners.add(mechs.get(i).name());
            }
        }
        return winners;
    }
 
    // Cards Card -> Cards
    // removes the first occurrence of the given card
    private Cards removeCard(Cards cards, Card target) {
        List<Card> list = new ArrayList<>(cards.getCards());
        list.remove(target);
        return new Cards(list);
    }
 
    // Cards Card -> Cards
    // appends the given card to the end
    private Cards addCard(Cards cards, Card card) {
        List<Card> list = new ArrayList<>(cards.getCards());
        list.add(card);
        return new Cards(list);
    }
}