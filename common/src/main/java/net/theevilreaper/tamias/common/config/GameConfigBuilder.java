package net.theevilreaper.tamias.common.config;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public final class GameConfigBuilder implements GameConfig.Builder {

    private int minPlayers;
    private int maxPlayers;
    private int lobbyTime;
    private int maxGameTime;
    private int teamSize;
    private int maxRounds;

    @Override
    public GameConfig.@NotNull Builder minPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
        return this;
    }

    @Override
    public GameConfig.@NotNull Builder maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    @Override
    public GameConfig.@NotNull Builder lobbyTime(int lobbyTime) {
        if (lobbyTime <= GameConfig.FORCE_START_TIME) {
            throw new IllegalArgumentException("Lobby time must be greater than " + GameConfig.FORCE_START_TIME);
        }
        this.lobbyTime = lobbyTime;
        return this;
    }

    @Override
    public GameConfig.@NotNull Builder gameTime(int gameTime) {
        this.maxGameTime = gameTime;
        return this;
    }

    @Override
    public GameConfig.@NotNull Builder teamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    @Override
    public GameConfig.@NotNull Builder maxRounds(int maxRounds) {
        int defaultRounds = InternalGameConfig.defaultConfig().maxRounds();
        Check.argCondition(maxRounds < defaultRounds, "The max rounds must be greater than " + defaultRounds);
        this.maxRounds = maxRounds;
        return this;
    }

    @Override
    public @NotNull GameConfig build() {
        return new GameConfigImpl(minPlayers, maxPlayers, lobbyTime, maxGameTime, teamSize, maxRounds);
    }
}
