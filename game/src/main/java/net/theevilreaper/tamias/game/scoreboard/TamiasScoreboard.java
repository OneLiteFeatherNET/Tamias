package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The interface represents the basic structure of a scoreboard for the game.
 * It is used to display different information to the player.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see TamiasBoard
 * @since 1.0.0
 */
public interface TamiasScoreboard extends Viewable {

    /**
     * Creates a new instance of the scoreboard
     *
     * @return the new instance
     */
    @Contract(pure = true)
    static @NotNull TamiasScoreboard of(int maxRounds) {
        return new TamiasBoard(maxRounds);
    }

    /**
     * Initializes the default values for the scoreboard
     */
    void initDefaults();

    /**
     * Resets the current board to the default values
     */
    void resetBoard();

    /**
     * Updates the time of the game
     *
     * @param time the current time
     */
    void updateTime(int time);

    void updateMapName(@NotNull String mapName);

    void updatePlayerCount(int playerCount);

    /**
     * Updates the component which represents the round.
     *
     * @param round the new data to set
     */
    void updateRound(@NotNull Component round);

    void updateTntCount(int tntCount);

    /**
     * Updates the game scoreboard with some default values for the round start
     *
     * @param tnt     the amount of tnt
     * @param players the amount of players
     * @param round   the current round
     */
    void updateGameDefaults(int tnt, int players, int round);

    /**
     * Switches the current board to the given type
     *
     * @param boardType the type of the board
     */
    void switchBoard(@NotNull BoardType boardType);

    /**
     * Updates the score of the given type with the given value
     *
     * @param scoreType the type of the score
     * @param value     the value to update
     */
    void updateScore(@NotNull ScoreType scoreType, @NotNull Component value);

    /**
     * The enum contains the different types of scores
     */
    enum BoardType {

        LOBBY,
        GAME;
    }

    /**
     * The score type represents the different types of scores which can be used in the game.
     *
     * @author theEvilReaper
     * @version 1.0.0
     * @since 1.0.0
     */
    enum ScoreType {

        TNT("tnt-count"),
        PLAYER("player-count"),
        ROUND("round-count");

        private final String name;

        /**
         * Constructs a new {@link ScoreType} with the given name and score.
         *
         * @param name the name of the score
         */
        ScoreType(@NotNull String name) {
            this.name = name;
        }

        /**
         * Returns the name of the score.
         *
         * @return the score name
         */
        public @NotNull String getName() {
            return name;
        }
    }

}
