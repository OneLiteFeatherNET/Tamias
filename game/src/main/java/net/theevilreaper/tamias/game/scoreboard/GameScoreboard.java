package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;

/**
 * The GameScoreboard class implements the {@link Scoreboard} interface and provides a specific layout for the game scoreboard.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public final class GameScoreboard implements Scoreboard, DefaultScoreLayout {

    private final Sidebar sidebar;

    /**
     * Constructs a new GameScoreboard with an empty header.
     */
    public GameScoreboard() {
        this.sidebar = new Sidebar(Component.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initDefaults() {
        this.initGameScoreboard(sidebar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTitle(Component title) {
        this.sidebar.setTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addViewer(Player player) {
        this.sidebar.addViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeViewer(Player player) {
        this.sidebar.removeViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateScore(ScoreType scoreType, Component component) {
        if (scoreType instanceof ScoreType.MapType) {
            throw new UnsupportedOperationException("Cannot update map score in GameScoreboard");
        }
        this.sidebar.updateLineContent(scoreType.key().asString(), component);
    }
}
