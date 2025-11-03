package net.theevilreaper.tamias.game.util.phase;

import net.theevilreaper.tamias.common.config.GameConfig;

import java.util.function.IntConsumer;

/**
 * Represents the data required for the lobby phase in a game.
 * This includes the time updater, maximum and minimum players, and the lobby time duration.
 *
 * @param timeUpdater a consumer that updates the time remaining in the lobby
 * @param maxPlayers  the maximum number of players allowed in the lobby
 * @param minPlayers  the minimum number of players required to start the game
 * @param lobbyTime   the duration of the lobby phase in seconds
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public record LobbyPhaseData(IntConsumer timeUpdater, int maxPlayers, int minPlayers, int lobbyTime) {

    /**
     * Creates a new LobbyPhaseData instance.
     *
     * @param timeUpdater the consumer to update the time
     * @param maxPlayers  the maximum number of players allowed in the lobby
     * @param minPlayers  the minimum number of players required to start the game
     * @param lobbyTime   the duration of the lobby phase in seconds
     */
    public LobbyPhaseData {
        if (minPlayers <= 0) {
            throw new IllegalArgumentException("minPlayers must be greater than 0");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException("maxPlayers must be greater than or equal to minPlayers");
        }
        if (lobbyTime <= 0) {
            throw new IllegalArgumentException("lobbyTime must be greater than 0");
        }
    }

    /**
     * Creates a new instance from the data class with the given values.
     *
     * @param timeUpdater the consumer to update to time
     * @param config      the {@link GameConfig} to get some values from it
     */
    public LobbyPhaseData(IntConsumer timeUpdater, GameConfig config) {
        this(timeUpdater, config.maxPlayers(), config.minPlayers(), config.lobbyTime());
    }
}
