package net.theevilreaper.tamias.common.config;

/**
 * The {@link InternalGameConfig} is the fallback configuration if no other configuration is available.
 * It provides default values for the game configuration.
 * These values should be only modified if the case is necessary and the default values are not suitable.
 *
 * @param minPlayers        the minimum number of players required to start a game
 * @param maxPlayers        the maximum number of players allowed in the game
 * @param lobbyTime         the time in seconds before the game starts
 * @param gameTime          the maximum duration of a game in minutes
 * @param teamSize          the general size of each team
 * @param maxRounds         the maximum rounds for the game
 * @param ticketMultiplier  tickets awarded per online player at round start
 * @param conversionRadius  radius, in blocks, scanned for Survivor-to-Bomber conversion
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
record InternalGameConfig(
        int minPlayers,
        int maxPlayers,
        int lobbyTime,
        int gameTime,
        int teamSize,
        int maxRounds,
        int ticketMultiplier,
        double conversionRadius
) implements GameConfig {

    /**
     * Returns the default configuration for the game.
     *
     * @return the default configuration
     */
    public static GameConfig defaultConfig() {
        return Instances.INTERNAL;
    }

    /**
     * Holds the reference to the default configuration instance.
     */
    static final class Instances {
        private static final int MAX_GAME_TIME = 900;
        private static final int LOBBY_PHASE_TIME = 30;
        private static final int MAX_PLAYERS = 13;
        private static final int MIN_PLAYERS = 1;
        private static final int GENERAL_TEAM_SIZE = 16;
        private static final int MAX_ROUNDS = 3;
        private static final int TICKET_MULTIPLIER = 5;
        private static final double CONVERSION_RADIUS = 4.0;

        static final GameConfig INTERNAL;

        static {
            INTERNAL = new InternalGameConfig(
                    MIN_PLAYERS,
                    MAX_PLAYERS,
                    LOBBY_PHASE_TIME,
                    MAX_GAME_TIME,
                    GENERAL_TEAM_SIZE,
                    MAX_ROUNDS,
                    TICKET_MULTIPLIER,
                    CONVERSION_RADIUS
            );
        }

        private Instances() {
            throw new UnsupportedOperationException("This class cannot be instantiated");
        }
    }
}


