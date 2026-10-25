package player;
 
import common.*;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/*
 * Shared search logic for both purchasing strategies.
 *
 * Both PurchasePointsStrategy and PurchaseSizeStrategy use the same
 * exchange and card purchase search -- the only difference is how
 * they compare two candidates to pick the best one. This class
 * contains the shared search logic so it only has to exist in one
 * place. Each subclass implements isBetter() to define what "best"
 * means for that strategy.
 *
 * We noticed the duplication after writing both strategy classes and
 * decided to refactor rather than fix bugs twice if something went wrong.
 */
abstract class AbstractStrategy implements Strategy {
 
    private static final int MAX_EXCHANGES = 4;
 
    // TurnState Equations -> TurnDecision
    // runs the search and returns the best decision according to
    // whichever isBetter() the subclass defines
    @Override
    public TurnDecision takeTurn(TurnState turn, Equations equations) {
        Pebbles wallet = turn.getActive().getWallet();
        Pebbles bank   = turn.getBank();
        Cards   cards  = turn.getVisibles();
 
        List<List<ExchangeStep>> allExchangeSeqs = new ArrayList<>();
        collectExchangeSequences(wallet, bank, equations,
            new ArrayList<>(), allExchangeSeqs);
 
        TurnDecision best = null;
        for (List<ExchangeStep> exchanges : allExchangeSeqs) {
            Pebbles walletAfter = applyExchanges(wallet, exchanges);
            List<TurnDecision> candidates =
                collectPurchaseCandidates(exchanges, walletAfter, cards);
            for (TurnDecision candidate : candidates) {
                if (best == null || isBetter(candidate, best)) {
                    best = candidate;
                } else if (!isBetter(best, candidate)) {
                    best = breakTie(best, candidate);
                }
            }
        }
 
        if (best == null) {
            best = new TurnDecision(
                new ArrayList<>(), new ArrayList<>(), 0, wallet);
        }
        return best;
    }
 
    // AbstractStrategy TurnDecision TurnDecision -> boolean
    // true if 'a' is strictly better than 'b' by this strategy's goal
    // subclasses implement this to define what "better" means
    abstract boolean isBetter(TurnDecision a, TurnDecision b);
 
    // List<ExchangeStep> -> Pebbles
    // applies a sequence of exchanges to the wallet and returns the result
    private Pebbles applyExchanges(Pebbles wallet,
                                    List<ExchangeStep> steps) {
        for (ExchangeStep step : steps) {
            wallet = wallet.remove(step.getGiven()).add(step.getReceived());
        }
        return wallet;
    }
 
    // Pebbles Pebbles Equations List<ExchangeStep> List<List<ExchangeStep>> -> void
    // recursively collects all legal exchange sequences up to MAX_EXCHANGES deep
    private void collectExchangeSequences(Pebbles wallet,
                                           Pebbles bank,
                                           Equations equations,
                                           List<ExchangeStep> current,
                                           List<List<ExchangeStep>> result) {
        result.add(new ArrayList<>(current));
        if (current.size() >= MAX_EXCHANGES) return;
 
        for (Equation eq : equations.getEquations()) {
            if (eq.canApplyLeftToRight(wallet, bank)) {
                ExchangeStep step = new ExchangeStep(eq, true);
                Pebbles nw = wallet.remove(eq.getLeft()).add(eq.getRight());
                Pebbles nb = bank.remove(eq.getRight()).add(eq.getLeft());
                current.add(step);
                collectExchangeSequences(nw, nb, equations, current, result);
                current.remove(current.size() - 1);
            }
            if (eq.canApplyRightToLeft(wallet, bank)) {
                ExchangeStep step = new ExchangeStep(eq, false);
                Pebbles nw = wallet.remove(eq.getRight()).add(eq.getLeft());
                Pebbles nb = bank.remove(eq.getLeft()).add(eq.getRight());
                current.add(step);
                collectExchangeSequences(nw, nb, equations, current, result);
                current.remove(current.size() - 1);
            }
        }
    }
 
    // List<ExchangeStep> Pebbles Cards -> List<TurnDecision>
    // returns all TurnDecisions reachable by purchasing some subset
    // of the visible cards after the given exchanges
    private List<TurnDecision> collectPurchaseCandidates(
            List<ExchangeStep> exchanges,
            Pebbles startWallet,
            Cards visibles) {
        List<TurnDecision> result = new ArrayList<>();
        collectPurchaseSequences(exchanges, visibles.getCards(),
            0, new ArrayList<>(), startWallet, 0, result);
        return result;
    }
 
    // recursive helper for collectPurchaseCandidates
    private void collectPurchaseSequences(
            List<ExchangeStep> exchanges,
            List<Card> available,
            int index,
            List<Card> purchased,
            Pebbles wallet,
            int points,
            List<TurnDecision> result) {
        result.add(new TurnDecision(
            exchanges, new ArrayList<>(purchased), points, wallet));
        for (int i = index; i < available.size(); i++) {
            Card card = available.get(i);
            if (wallet.hasAtLeast(card.getPebbles())) {
                Pebbles nw  = wallet.remove(card.getPebbles());
                int     pts = TurnDecision.score(card, nw.size());
                purchased.add(card);
                collectPurchaseSequences(exchanges, available, i + 1,
                    purchased, nw, points + pts, result);
                purchased.remove(purchased.size() - 1);
            }
        }
    }
 
    // TurnDecision TurnDecision -> TurnDecision
    // the game's tie-breaking rules:
    //   1. fewer exchanges wins
    //   2. smaller card sequence wins
    //   3. smaller exchange sequence wins
    private TurnDecision breakTie(TurnDecision a, TurnDecision b) {
        int ea = a.getExchanges().size();
        int eb = b.getExchanges().size();
        if (ea != eb) return ea < eb ? a : b;
        int cardCmp = compareCardSequences(a.getPurchases(), b.getPurchases());
        if (cardCmp != 0) return cardCmp < 0 ? a : b;
        int exchCmp = compareExchangeSequences(
            a.getExchanges(), b.getExchanges());
        return exchCmp <= 0 ? a : b;
    }
 
    // List<Card> List<Card> -> int
    // compares two card sequences lexicographically
    // returns negative if a < b, positive if a > b, 0 if equal
    private int compareCardSequences(List<Card> a, List<Card> b) {
        if (a.size() != b.size()) return a.size() - b.size();
        for (int i = 0; i < a.size(); i++) {
            int cmp = compareCards(a.get(i), b.get(i));
            if (cmp != 0) return cmp;
        }
        return 0;
    }
 
    // Card Card -> int
    // non-starred < starred; equal star status -> compare pebble bags
    private int compareCards(Card a, Card b) {
        if (!a.hasStar() && b.hasStar()) return -1;
        if (a.hasStar() && !b.hasStar()) return 1;
        return comparePebbles(a.getPebbles(), b.getPebbles());
    }
 
    // Pebbles Pebbles -> int
    // smaller size wins; equal size -> compare sorted abbreviation strings
    // this is the game's standard ordering for pebble collections
    private int comparePebbles(Pebbles a, Pebbles b) {
        if (a.size() != b.size()) return a.size() - b.size();
        return toSortedString(a).compareTo(toSortedString(b));
    }
 
    // Pebbles -> String
    // returns a sorted string of pebble abbreviations
    // e.g. {RED:2, BLUE:1} -> "RRB"
    private String toSortedString(Pebbles pebbles) {
        List<String> chars = new ArrayList<>();
        for (Pebble p : pebbles.toList()) {
            chars.add(p.abbreviation());
        }
        Collections.sort(chars);
        StringBuilder sb = new StringBuilder();
        for (String c : chars) sb.append(c);
        return sb.toString();
    }
 
    // List<ExchangeStep> List<ExchangeStep> -> int
    // compares two exchange sequences lexicographically
    private int compareExchangeSequences(List<ExchangeStep> a,
                                          List<ExchangeStep> b) {
        if (a.size() != b.size()) return a.size() - b.size();
        for (int i = 0; i < a.size(); i++) {
            int cmp = compareExchangeSteps(a.get(i), b.get(i));
            if (cmp != 0) return cmp;
        }
        return 0;
    }
 
    // ExchangeStep ExchangeStep -> int
    // compares given pebbles first, then received pebbles
    private int compareExchangeSteps(ExchangeStep a, ExchangeStep b) {
        int leftCmp = comparePebbles(a.getGiven(), b.getGiven());
        if (leftCmp != 0) return leftCmp;
        return comparePebbles(a.getReceived(), b.getReceived());
    }
}