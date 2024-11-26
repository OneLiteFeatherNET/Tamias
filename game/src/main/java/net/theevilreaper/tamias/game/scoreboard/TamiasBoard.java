package net.theevilreaper.tamias.game.scoreboard;

import de.icevizion.aves.util.Strings;
import de.icevizion.aves.util.TimeFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class TamiasBoard implements TamiasScoreboard, DefaultScoreLayout {

    private final Supplier<String> mapName;
    private final Sidebar lobbyScoreboard;
    private final Sidebar gameScoreboard;
    private final Set<Player> viewers;
    private final Component timeComponent = Component.text("Time:", NamedTextColor.GRAY).append(Component.space());
    private Sidebar currentScoreboard;
    private BoardType boardType;

    TamiasBoard(@NotNull Supplier<String> mapName) {
        this.mapName = mapName;
        this.viewers = new HashSet<>();
        this.boardType = BoardType.LOBBY;
        this.lobbyScoreboard = new Sidebar(Component.empty());
        this.gameScoreboard = new Sidebar(Component.empty());
        this.currentScoreboard = this.lobbyScoreboard;
    }

    @Override
    public void initDefaults() {
        this.initLobbyScoreboard(this.lobbyScoreboard, this.mapName.get());
        this.initGameScoreboard(this.gameScoreboard);
    }

    @Override
    public void updateGameDefaults(int tnt, int players, int round) {
        this.gameScoreboard.updateLineContent(ScoreType.TNT.getName(), Component.text(String.valueOf(tnt)));
        this.gameScoreboard.updateLineContent(ScoreType.PLAYER.getName(), Component.text(String.valueOf(players)));
        this.gameScoreboard.updateLineContent(ScoreType.ROUND.getName(), Component.text(String.valueOf(round)));
    }

    @Override
    public void resetBoard() {
        this.viewers.forEach(this.currentScoreboard::removeViewer);
        this.currentScoreboard = null;
        this.boardType = null;
    }

    @Override
    public void updateTime(int time) {
        if (this.currentScoreboard == null) return;
        Component timeFormat = Component.text(Strings.getTimeString(TimeFormat.MM_SS, time));
        this.currentScoreboard.setTitle(this.timeComponent.append(timeFormat));
    }

    @Override
    public void switchBoard(@NotNull BoardType boardType) {
        if (this.boardType == boardType) return;
        this.boardType = boardType;
        this.viewers.forEach(this.currentScoreboard::removeViewer);
        if (this.boardType == BoardType.LOBBY) {
            this.currentScoreboard = this.lobbyScoreboard;
        } else {
            this.currentScoreboard = this.gameScoreboard;
        }
        this.viewers.forEach(this.currentScoreboard::addViewer);
    }

    @Override
    public void updateScore(@NotNull ScoreType scoreType, @NotNull Component value) {
        if (this.boardType == null || this.boardType == BoardType.LOBBY) return;
        this.gameScoreboard.updateLineContent(scoreType.getName(), value);
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        if (this.viewers.add(player)) {
            this.currentScoreboard.addViewer(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        if (this.viewers.remove(player)) {
            this.currentScoreboard.removeViewer(player);
            return true;
        }
        return false;
    }

    @Contract(pure = true)
    @Override
    public @NotNull @UnmodifiableView Set<@NotNull Player> getViewers() {
        return Collections.unmodifiableSet(this.viewers);
    }
}
