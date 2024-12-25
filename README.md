# SuicideTNT

SuicideTNT is a round based game where you have a survivor and a bomber team. Each of them have the goal to win a round
but with different conditions. A survivor must survivor the given time of a round while a bomber must blow up each
survivor. If a team reaches this goal the current round is over and a new round starts. The game ends after a given
amount of rounds.

## Game

If the server runs the game, it doesn't serve a bunch of commands to the player.
There is only a command to force start the game, if a specific condition is not reached.

The mentioned command is:

- `/start` - Forces the game to start

## Setup

The game has a dedicated setup mode to set up maps for the game.
It is encapsulated from the game itself to prevent any interference with the game.

The setup has the following command with the following subcommands:

- `/setup name <name>` - Sets the name of a map
- `/setup builders <builder>` - Sets the builders of a map
- `/setup area <left,right>` - Sets the area of a map
- `/setup position <option>` - Sets the spawn for different components of the game
