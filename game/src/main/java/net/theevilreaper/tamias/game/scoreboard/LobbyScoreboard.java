package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import static net.theevilreaper.tamias.game.scoreboard.ScoreType.PLAYER;

/**
 * The LobbyScoreboard class implements the {@link Scoreboard} interface and provides a specific layout for the lobby scoreboard.
 * It initializes the scoreboard with default values and allows updating scores and titles.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public final class LobbyScoreboard implements Scoreboard, DefaultScoreLayout {

    private final Sidebar sidebar;

    /**
     * Constructs a new LobbyScoreboard with the specified display name.
     *
     * @param displayName the display name for the scoreboard
     */
    public LobbyScoreboard(@NotNull Component displayName) {
        this.sidebar = new Sidebar(displayName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTitle(@NotNull Component title) {
        this.sidebar.setTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initDefaults() {
        this.initLobbyScoreboard(sidebar, "not-set");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addViewer(@NotNull Player player) {
        if (this.sidebar.addViewer(player)) {
            int size = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            Component component = Component.text(size, NamedTextColor.YELLOW);
            this.updateScore(PLAYER, component);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeViewer(@NotNull Player player) {
        if (this.sidebar.removeViewer(player)) {
            int size = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
            Component component = Component.text(size, NamedTextColor.YELLOW);
            this.updateScore(PLAYER, component);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateScore(@NotNull ScoreType scoreType, @NotNull Component component) {
        if (scoreType instanceof ScoreType.TNTCount || scoreType instanceof ScoreType.RoundType) {
            throw new UnsupportedOperationException("Cannot update TNT or ROUND scores in LobbyScoreboard");
        }
        this.sidebar.updateLineContent(scoreType.key().asString(), component);
    }
}
