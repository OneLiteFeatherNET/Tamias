package net.theevilreaper.tamias.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public final class BoardHelper {

    private static final String LINE_PREFIX = "TNT-";

    private final Sidebar scoreboard;

    public BoardHelper() {
        this.scoreboard = new Sidebar(Component.empty());
    }

    public void initLobbyLayout(@NotNull Component displayName) {
        this.scoreboard.setTitle(displayName);
    }

    public void updateTitle(@NotNull Component displayName) {
        this.scoreboard.setTitle(displayName);
    }

    public void initGameLayout(int maxRounds) {
        int idCounter = 1;
        this.scoreboard.createLine(new Sidebar.ScoreboardLine(LINE_PREFIX + idCounter, Component.text("Round:"), idCounter));
        this.scoreboard.createLine(new Sidebar.ScoreboardLine(LINE_PREFIX + ++idCounter, Component.text("0/" + maxRounds), idCounter));
    }

    public void addViewer(@NotNull Player player) {
        this.scoreboard.addViewer(player);
    }

    public void removeViewer(@NotNull Player player) {
        this.scoreboard.removeViewer(player);
    }
}
