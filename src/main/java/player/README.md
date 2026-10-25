# player
 
The strategy and mechanism components for a Bazaar player. The referee
and common packages should never import from here -- the referee only
cares about the result of a turn, not how it was computed.
 
## Files
 
**Strategy.java** -- the interface both strategies implement. One method:
takeTurn(TurnState, Equations) -> TurnDecision. Having this as an interface
means the player mechanism can swap strategies without changing
anything else.
 
**AbstractStrategy.java** -- the search both strategies share: every legal
exchange sequence up to four steps, every affordable set of purchases after
each one, and the tie-breaking rules. Subclasses only define isBetter(), what
makes one candidate turn better than another.

**PurchasePointsStrategy.java** -- picks the exchanges and purchases that
maximize total points this turn.

**PurchaseSizeStrategy.java** -- picks the exchanges and purchases that
maximize the number of cards bought this turn.
 
**TurnDecision.java** -- the result a strategy returns: the chosen exchange
sequence, card purchases, total points, and the wallet state after all moves.
Also has a score() helper that looks up points in the rule book's scoring table.
 
**ExchangeStep.java** -- one step in an exchange sequence: an equation plus
a direction (left-to-right or right-to-left). Needed because an equation is
bidirectional so we have to track which way it was applied.
 
**Mechanism.java** -- wraps a strategy and implements the protocol the referee
uses to interact with a player. Has a name, delegates decisions to the strategy,
and exposes setup(), wantsPebble(), requestExchanges(), requestPurchases(), and
win() so the referee can run a game without knowing anything about how the
strategy works.