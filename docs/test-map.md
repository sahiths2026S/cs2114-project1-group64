# JUnit coverage map

Run `./scripts/test.sh --details=summary` from the repository root. Tests use known
pill lists and in-memory input/output, so they do not require a person typing or
rely on a lucky random order.

| Area | Normal case | Bad input / boundary |
| --- | --- | --- |
| Game construction | Two healthy players, nonempty queue, Player 1 first | Null dependencies/elements and invalid health rejected, empty game finished |
| `startTurn`, `play` | Known input consumes pills and completes a game | Bad input retries without state changes, 10,000 retries, EOF exits |
| `takePill`, `givePill` | Right recipient, cyanide damage, sugar no damage, turn changes | Empty queue and post-game actions leave state unchanged |
| `display` | State prints without consuming input | Finished game prints result without another prompt |
| `isGameOver` | Healthy players and remaining pills keep game active | Zero health or empty queue ends game |
| Remaining counts | Mixed queue counts each type | Empty queue reports zero, consumed pills disappear |
| Player construction and health | Health starts and updates correctly | Negative setup/damage rejected, zero accepted, overkill clamps at zero |
| Pill construction and identity | Cyanide is true | Sugar is false (there is no invalid boolean) |
| Input reading | Complete input line returned | Blank/EOF/closed scanner handled, null scanner rejected |
| Input validation | `1`, `2`, optional surrounding whitespace | Null, non-string object, blank, decimal, negative, mixed/unsupported command rejected |
| UI status and results | Health/counts/turn/result readable | Empty and active result calls safe, null dependencies rejected |
| UI errors/tutorial | Repeated messages and strategy text print | No game state mutation, invalid result arguments rejected |
| Main options | Help, tutorial, demo, seed launch correctly | Unknown/missing/malformed options and null dependencies rejected safely |

Invalid setup is a programming error and raises a controlled exception. Invalid
commands typed during play produce a friendly message and retry. These are
different boundaries, and tests check both.
