package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The GameScoreboard class implements the {@link Scoreboard} interface and provides a specific layout for the game scoreboard.
 * <p>
 * Each viewer gets an own {@link Sidebar}, so a line like the round wins can differ per player. Shared values are
 * stored and applied to every sidebar, including the ones of viewers which are added later.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public final class GameScoreboard implements Scoreboard, DefaultScoreLayout {

    private final Map<UUID, Sidebar> sidebars;
    private final Map<String, Component> sharedLines;
    private Component title;

    /**
     * Constructs a new GameScoreboard with an empty header.
     */
    public GameScoreboard() {
        this.sidebars = new ConcurrentHashMap<>();
        this.sharedLines = new ConcurrentHashMap<>();
        this.title = Component.empty();
    }

    /**
     * {@inheritDoc}
     * The sidebars are initialized for each viewer, so there is nothing to do here.
     */
    @Override
    public void initDefaults() {
        // Each sidebar is initialized when its viewer is added
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTitle(Component title) {
        this.title = title;
        this.sidebars.values().forEach(sidebar -> sidebar.setTitle(title));
    }

    /**
     * {@inheritDoc}
     * Adding a player which already is a viewer keeps its sidebar.
     */
    @Override
    public void addViewer(Player player) {
        Sidebar sidebar = this.sidebars.computeIfAbsent(player.getUuid(), _ -> createSidebar());
        sidebar.addViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeViewer(Player player) {
        Sidebar sidebar = this.sidebars.remove(player.getUuid());
        if (sidebar == null) return;
        sidebar.removeViewer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateScore(ScoreType scoreType, Component component) {
        if (scoreType instanceof ScoreType.MapType) {
            throw new UnsupportedOperationException("Cannot update map score in GameScoreboard");
        }
        String lineId = scoreType.key().asString();
        Component content = SPACER.append(component);
        this.sharedLines.put(lineId, content);
        this.sidebars.values().forEach(sidebar -> sidebar.updateLineContent(lineId, content));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateScore(Player player, ScoreType scoreType, Component component) {
        Sidebar sidebar = this.sidebars.get(player.getUuid());
        if (sidebar == null) return;
        sidebar.updateLineContent(scoreType.key().asString(), SPACER.append(component));
    }

    /**
     * Returns the sidebar of the given player.
     *
     * @param player the player to get the sidebar for
     * @return the sidebar or null if the player is no viewer
     */
    @Nullable Sidebar getSidebar(Player player) {
        return this.sidebars.get(player.getUuid());
    }

    private Sidebar createSidebar() {
        Sidebar sidebar = new Sidebar(this.title);
        this.initGameScoreboard(sidebar);
        this.sharedLines.forEach(sidebar::updateLineContent);
        return sidebar;
    }
}
