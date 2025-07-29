package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link ScoreType} interface represents different types of scores that can be displayed on the scoreboard.
 * Each score type has a unique key identifier that can be used to reference it.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public interface ScoreType {

    PlayerCount PLAYER = new PlayerCount();
    TNTCount TNT = new TNTCount();
    RoundType ROUND_TYPE = new RoundType();
    MapType MAP_TYPE = new MapType();


    /**
     * Returns the identifier of the score type.
     *
     * @return the identifier
     */
    @NotNull Key key();

    /**
     * Represents the player score type.
     */
    record TNTCount() implements ScoreType {
        @Override
        public @NotNull Key key() {
            return Key.key("tamias:tnt-count");
        }
    }

    /**
     * Represents the player score type.
     */
    record PlayerCount() implements ScoreType {
        @Override
        public @NotNull Key key() {
            return Key.key("tamias:player-count");
        }
    }

    /**
     * Represents the round score type.
     */
    record RoundType() implements ScoreType {
        @Override
        public @NotNull Key key() {
            return Key.key("tamias:round");
        }
    }

    /**
     * Represents the map score type.
     */
    record MapType() implements ScoreType {
        @Override
        public @NotNull Key key() {
            return Key.key("tamias:map");
        }
    }
}
