# referee
 
The referee's private components -- the game state and the referee
itself. Nothing in common or player should import from here.
 
## Files
 
**GameState.java** -- the full private state of a running game: bank,
visible cards, face-down deck, and all players in turn order. Supports
isGameOver(), toTurnState(), and a set of transformation methods the
referee uses to apply legal actions without mutating the existing state.
 
**Referee.java** -- runs a complete game by granting players turns one
at a time until the game is over. Calls the player mechanism twice per
turn (first action, then card purchases), checks legality via RuleBook,
and eliminates any player that misbehaves. Returns a GameResult with
the winners and the misbehaving players.
 
**GameResult.java** -- the result of a completed game. Holds the names
of the winners and the names of the players that were eliminated during
the game. Simple data class, no logic.