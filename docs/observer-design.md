# Design: Game Observer

A proposed extension, not yet implemented: an observer that watches a running game and displays each state change.

## What This Is
 
An observer watches a game as it runs and renders what it sees. The
referee tells it about each state change. The observer never participates
-- it just watches. This is useful for demos where someone wants to see
the game without reading code or log output.
 
## The Interface
 
An observer implements this interface:
 
```java
public interface GameObserver {
 
    // GameObserver GameState -> void
    // called after each legal state change during the game
    void update(GameState state);
 
    // GameObserver GameResult -> void
    // called once when the game ends
    void gameOver(GameResult result);
}
```
 
That's it. The referee calls update() after every successful action
(pebble draw, exchanges, card purchases) and gameOver() at the end.
The observer just receives information -- it never responds and the
referee never waits for it.
 
## How It Connects to the Referee
 
The referee gets an optional observer at construction:
 
```java
// existing constructor -- works as before
public Referee(Equations equations) { ... }
 
// new constructor -- same behavior plus observer notifications
public Referee(Equations equations, GameObserver observer) { ... }
```
 
If no observer is passed, the referee behaves exactly as it does now.
All existing tests continue to work.
 
Inside runGame(), the referee calls observer.update(state) after each
state change, wrapped in a try/catch. If the observer throws, the referee
ignores it and keeps going -- a crashing observer shouldn't take down
the game. Same policy as a crashing player.
 
## State Transitions the Observer Sees

The referee calls `update(state)` after every successful state change during a game. Here is what each transition looks like from the observer's perspective.

**Pebble draw.** A player requested a pebble from the bank. The bank shrinks by one pebble and the active player's wallet grows by one. No cards change. The observer sees the new state immediately after the pebble is added.

**Exchange.** A player traded pebbles with the bank using one equation step. The active player's wallet and the bank both change by the two sides of that equation. The observer receives a `update` call after each individual exchange step if the player performs multiple steps, so it sees the game evolve one trade at a time.

**Card purchase.** A player bought one or more visible cards. The active player's wallet shrinks by the card's pebble cost, the player's score increases, and the purchased card disappears from the visible set. If the deck is non-empty, a new card replaces it. The observer sees the fully updated state after all purchases in that turn are applied.

**Player elimination.** A player misbehaved (threw an exception, returned an illegal action, or returned null). The referee removes that player from the game entirely. Their pebbles are not returned to the bank. The observer sees the state after removal, with one fewer entry in the players list.

**Game end.** When the game reaches a terminal state—a player hits 20 points, all cards are gone, all players are eliminated, or a stalemate is detected—the referee stops granting turns and calls `gameOver(result)` once. The result carries the final winner names and the names of all eliminated players. The observer does not receive any further `update` calls after `gameOver`.
 
A text-based observer implementation might print something like this
to the console after each update:
 
```
--- Turn 3: Alice ---
Bank:    R R W B B G
Cards:   [R R W B G]  [W W B B Y]  [R G G Y Y *]
Alice:   wallet=[R R B]  score=2
Bob:     score=5
 
Alice exchanges R -> B
Alice buys [R R W B G]  (+1 pt)
Alice:   wallet=[R B B]  score=3
```
 
A graphical observer would render the same data visually. The interface
is the same either way.
 
## A Few Design Notes
 
The observer receives GameState snapshots. Since GameState is immutable,
the observer can't accidentally corrupt the running game even if it does
something weird with the state it receives.
 
This design only supports one observer. If multiple observers were needed
later, the referee could hold a List<GameObserver> instead -- but one is
enough for the demo use case right now.
 
The observer interface lives in the referee package since the referee
is the one calling it. A separate observer implementation (like the
text-based one above) would live wherever the person building it wants
to put it, as long as it implements the interface.