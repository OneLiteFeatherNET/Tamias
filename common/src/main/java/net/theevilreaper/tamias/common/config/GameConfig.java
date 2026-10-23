package net.theevilreaper.tamias.common.config;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link GameConfig} interface represents the structure for a configuration which is used by the game.
 * It contains some values that can be adjusted to change specific settings for the game.
 * There are also some static values in the interface that are also used in the game.
 * Each static value indicates that it is a constant value and should not be changed.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public sealed interface GameConfig permits GameConfigImpl, InternalGameConfig {

    Key BOMBER_KEY = Key.key("tamias", "bomber");
    Key SURVIVOR_KEY = Key.key("tamias", "survivor");

    /* The name of the Slender team.
     */
    String BOMBER_TEAM = "Bomber";
    /**
     * The name of the Survivor team.
     */
    String SURVIVOR_TEAM_NAME = "Survivor";

    String MAP_FILE_NAME = "map.json";

    String MAP_FOLDER = "maps";

    int FORCE_START_TIME = 11;

    /**
     * Creates a new {@link Builder} which can be used to create a new game configuration.
     *
     * @return the builder instance
     */
    @Contract(pure = true)
    static Builder builder() {
        return new GameConfigBuilder();
    }

    /**
     * Returns the minimum number of players required to start a game.
     *
     * @return the minimum number of players
     */
    int minPlayers();

    /**
     * Returns the maximum number of players allowed in the game.
     *
     * @return the maximum number of players
     */
    int maxPlayers();

    /**
     * Returns the lobby time in seconds.
     *
     * @return the lobby time
     */
    int lobbyTime();

    /**
     * Returns the duration of a round in seconds.
     *
     * @return the duration of a round
     */
    int gameTime();

    /**
     * Returns the size for each team.
     *
     * @return the given size
     */
    int teamSize();

    /**
     * Returns the maximum rounds for the game.
     *
     * @return the maximum rounds
     */
    int maxRounds();

    /**
     * Returns the DSN Sentry reports to.
     *
     * @return the DSN, or {@code null} to keep the error reporting off
     */
    @Nullable String sentryDsn();

    /**
     * The {@link Builder} interface is used to create a new game configuration.
     * It provides methods to set the values for the configuration.
     *
     * @author theEvilReaper
     * @version 1.0.0
     * @since 1.0.0
     */
    sealed interface Builder permits GameConfigBuilder {

        /**
         * Sets the minimum number of players required to start a game.
         *
         * @param minPlayers the minimum number of players
         * @return the builder instance
         */
        Builder minPlayers(int minPlayers);

        /**
         * Sets the maximum number of players allowed in the game.
         *
         * @param maxPlayers the maximum number of players
         * @return the builder instance
         */
        Builder maxPlayers(int maxPlayers);

        /**
         * Sets the lobby time in seconds.
         *
         * @param lobbyTime the lobby time
         * @return the builder instance
         * @throws IllegalArgumentException if the lobby time is than the {@link GameConfig#FORCE_START_TIME}
         */
        Builder lobbyTime(int lobbyTime);

        /**
         * Sets the duration of a round in seconds.
         *
         * @param gameTime the duration of a round
         * @return the builder instance
         */
        Builder gameTime(int gameTime);

        /**
         * Sets the general size for each team.
         *
         * @param teamSize the size of the team
         * @return the builder instance
         */
        Builder teamSize(int teamSize);

        /**
         * Sets the maximum rounds for the game.
         *
         * @param maxRounds the maximum rounds
         * @return the builder instance
         */
        Builder maxRounds(int maxRounds);

        /**
         * Sets the DSN Sentry reports to.
         *
         * @param sentryDsn the DSN, or {@code null} to keep the error reporting off
         * @return the builder instance
         */
        Builder sentryDsn(@Nullable String sentryDsn);

        /**
         * Builds the game configuration.
         *
         * @return the created configuration
         */
        GameConfig build();
    }
}
