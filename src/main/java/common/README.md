# common
 
Shared data representations used by both the referee and the player.
Nothing in here should depend on referee or player -- if it does,
something is wrong with the design.
 
The reason this package exists is that both sides need to talk about
the same things (pebbles, cards, equations, turn state) without one
depending on the other. Putting shared stuff in common avoids that.
 
## Files
 
**Pebble.java** -- the five colors (RED, WHITE, BLUE, GREEN, YELLOW).
Everything else builds on this.
 
**Pebbles.java** -- a multiset of pebbles. Represents wallets, the bank,
and equation sides. Immutable -- add() and remove() return new instances.
 
**Equation.java** -- one bidirectional trade between two disjoint pebble
groups. equals() treats both orientations as the same equation since
that's what bidirectional means.
 
**Equations.java** -- the table of up to 10 equations for a game.
filterApplicable() is the main thing the player uses each turn.
 
**Card.java** -- one card with exactly 5 pebbles and an optional star.
 
**Cards.java** -- an ordered list of cards. Used for both the visible
cards and the face-down deck.
 
**PlayerState.java** -- one player's wallet and score. Lives here (not
in referee) because TurnState also needs it and TurnState can't import
from referee.
 
**TurnState.java** -- what the referee sends to the active player at
the start of their turn. Only includes what the player is allowed to
know -- other players' scores but not their wallets.
 
**RuleBook.java** -- the rules of the game. Both the referee and the
player mechanism need to check legality, so this has to live here
rather than in referee. Contains legality checks for exchanges, pebble
requests, and card purchases, plus the scoring table and a deterministic
pebble picker for testing.