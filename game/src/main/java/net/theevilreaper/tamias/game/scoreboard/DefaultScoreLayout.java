package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import static net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard.ScoreType.*;

/**
 * The interface contains methods, to load a specific default layout for a {@link Sidebar}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see TamiasBoard
 * @since 1.0.0
 */
sealed interface DefaultScoreLayout permits TamiasBoard {

    Component SPACER = Component.text("»", NamedTextColor.YELLOW).append(Component.space());

    /**
     * Initializes the lobby scoreboard with the given map name
     *
     * @param lobbyScoreboard the scoreboard to initialize
     * @param mapName         the name of the map
     */
    default void initLobbyScoreboard(@NotNull Sidebar lobbyScoreboard, @NotNull String mapName) {
        lobbyScoreboard.createLine(new Sidebar.ScoreboardLine("empty-line-1", Component.empty(), 6));
        lobbyScoreboard.createLine(new Sidebar.ScoreboardLine("player-header", Component.text("Players:", NamedTextColor.GRAY), 5));
        lobbyScoreboard.createLine(new Sidebar.ScoreboardLine(PLAYER.getName(), Component.text("0", NamedTextColor.YELLOW), 4));
        lobbyScoreboard.createLine(new Sidebar.ScoreboardLine("empty-line-2", Component.empty(), 3));
        lobbyScoreboard.createLine(new Sidebar.ScoreboardLine("map-header", Component.text("Map:", NamedTextColor.GRAY), 2));
        lobbyScoreboard.createLine(new Sidebar.ScoreboardLine("map-name", SPACER.append(Component.text(mapName)), 1));
    }

    /**
     * Initializes the game scoreboard with the default values
     *
     * @param gameScoreboard the scoreboard to initialize
     */
    default void initGameScoreboard(@NotNull Sidebar gameScoreboard) {
        gameScoreboard.createLine(new Sidebar.ScoreboardLine("empty-line-1", Component.empty(), 8));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine("tnt-header", Component.text("TNT:"), 7));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine(TNT.getName(), Component.text("0"), 6));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine("empty-line-2", Component.empty(), 5));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine("player-header", Component.text("Players:"), 4));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine(PLAYER.getName(), Component.text("0"), 3));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine("empty-line-3", Component.empty(), 2));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine("round-header", Component.text("Round:"), 1));
        gameScoreboard.createLine(new Sidebar.ScoreboardLine(ROUND.getName(), Component.text("0"), 0));
    }
}
