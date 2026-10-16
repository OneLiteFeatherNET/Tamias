package net.theevilreaper.tamias.game.scoreboard;

import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.Nullable;

/**
 * Gives tests outside the scoreboard package access to the sidebar of a {@link GameScoreboard} viewer.
 */
public final class GameScoreboardAccess {

    public static @Nullable Sidebar sidebar(GameScoreboard gameScoreboard, Player player) {
        return gameScoreboard.getSidebar(player);
    }

    private GameScoreboardAccess() {
    }
}
