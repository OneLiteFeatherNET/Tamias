package net.theevilreaper.tamias.common.config;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

public final class GameConfigBuilder implements GameConfig.Builder {

    /**
     * A round needs at least one bomber and one survivor.
     */
    private static final int MIN_GAME_PLAYERS = 2;

    private int minPlayers;
    private int maxPlayers;
    private int lobbyTime;
    private int maxGameTime;
    private int teamSize;
    private int maxRounds;
    private @Nullable String sentryDsn;

    @Override
    public GameConfig.Builder minPlayers(int minPlayers) {
        Check.argCondition(minPlayers < MIN_GAME_PLAYERS, "The min players must be at least " + MIN_GAME_PLAYERS);
        this.minPlayers = minPlayers;
        return this;
    }

    @Override
    public GameConfig.Builder maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    @Override
    public GameConfig.Builder lobbyTime(int lobbyTime) {
        if (lobbyTime <= GameConfig.FORCE_START_TIME) {
            throw new IllegalArgumentException("Lobby time must be greater than " + GameConfig.FORCE_START_TIME);
        }
        this.lobbyTime = lobbyTime;
        return this;
    }

    @Override
    public GameConfig.Builder gameTime(int gameTime) {
        this.maxGameTime = gameTime;
        return this;
    }

    @Override
    public GameConfig.Builder teamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    @Override
    public GameConfig.Builder maxRounds(int maxRounds) {
        int defaultRounds = InternalGameConfig.defaultConfig().maxRounds();
        Check.argCondition(maxRounds < defaultRounds, "The max rounds must be greater than " + defaultRounds);
        this.maxRounds = maxRounds;
        return this;
    }

    @Override
    public GameConfig.Builder sentryDsn(@Nullable String sentryDsn) {
        this.sentryDsn = sentryDsn == null || sentryDsn.isBlank() ? null : sentryDsn.strip();
        return this;
    }

    @Override
    public GameConfig build() {
        // Every player can end up in the same team, e.g. when all survivors are converted to bombers
        Check.argCondition(teamSize < maxPlayers, "The team size must be at least the max players");
        return new GameConfigImpl(minPlayers, maxPlayers, lobbyTime, maxGameTime, teamSize, maxRounds, sentryDsn);
    }
}
