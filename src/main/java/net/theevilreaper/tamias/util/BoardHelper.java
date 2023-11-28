package net.theevilreaper.tamias.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.scoreboard.Scoreboard;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public final class BoardHelper {

    private final Scoreboard scoreboard;

    public BoardHelper(@NotNull Component displayName) {
        this.scoreboard = new Sidebar(displayName);
    }

    public void initLobbyLayout() {
        var packet = this.scoreboard.getCreationObjectivePacket(Component.empty(), ScoreboardObjectivePacket.Type.INTEGER);

    }

    public void initGameLayout() {

    }

    public void addViewer(@NotNull Player player) {
        this.scoreboard.addViewer(player);
    }

    public void removeViewer(@NotNull Player player) {
        this.scoreboard.removeViewer(player);
    }
}
