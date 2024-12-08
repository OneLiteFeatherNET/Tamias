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

import static net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard.ScoreType.PLAYER;
import static net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard.ScoreType.ROUND;
import static net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard.ScoreType.TNT;

public final class TamiasBoard implements TamiasScoreboard, DefaultScoreLayout {

    private final Component timeComponent = Component.text("Time:", NamedTextColor.GOLD).append(Component.space());
    private final Component bracketSpacer = Component.space().append(Component.text("|", NamedTextColor.GRAY)).append(Component.space());
    private final Component lobbyTimeComponent = Component.text("Lobby", NamedTextColor.GREEN)
            .append(bracketSpacer)
            .append(timeComponent);

    private final Sidebar lobbyScoreboard;
    private final Sidebar gameScoreboard;
    private final Set<Player> viewers;
    private Sidebar currentScoreboard;
    private BoardType boardType;

    private final int maxRound;

    TamiasBoard(int maxRound) {
        this.viewers = new HashSet<>();
        this.boardType = BoardType.LOBBY;
        this.lobbyScoreboard = new Sidebar(Component.empty());
        this.gameScoreboard = new Sidebar(Component.empty());
        this.currentScoreboard = this.lobbyScoreboard;
        this.maxRound = maxRound;
    }

    @Override
    public void initDefaults() {
        this.initLobbyScoreboard(this.lobbyScoreboard, "not-set");
        this.initGameScoreboard(this.gameScoreboard);
    }

    @Override
    public void updateMapName(@NotNull String mapName) {
        if (this.boardType != BoardType.LOBBY) return;
        this.currentScoreboard.updateLineContent("map-name", SPACER.append(Component.text(mapName, NamedTextColor.RED)));
    }

    @Override
    public void updatePlayerCount(int playerCount) {
        Component playerComponent = Component.text(String.valueOf(playerCount), NamedTextColor.WHITE);
        this.currentScoreboard.updateLineContent(PLAYER.getName(), SPACER.append(playerComponent));
    }

    @Override
    public void updateGameDefaults(int tnt, int players, int round) {
        this.updateTntCount(tnt);
        this.updatePlayerCount(players);
        this.updateRound(SPACER);
    }

    @Override
    public void updateRound(@NotNull Component round) {
        if (this.boardType == BoardType.LOBBY) return;
        this.currentScoreboard.updateLineContent(ROUND.getName(), SPACER.append(round));
    }

    @Override
    public void updateTntCount(int tntCount) {
        if (this.boardType == BoardType.LOBBY) return;
        Component newTntCount = SPACER.append(Component.text(String.valueOf(tntCount), NamedTextColor.WHITE));
        this.currentScoreboard.updateLineContent(TNT.getName(), newTntCount);
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
        Component timeFormat = Component.text(Strings.getTimeString(TimeFormat.MM_SS, time), NamedTextColor.YELLOW);
        Component rawTimeFormat = this.boardType == BoardType.LOBBY ? lobbyTimeComponent : timeComponent;
        this.currentScoreboard.setTitle(rawTimeFormat.append(timeFormat));
    }

    @Override
    public void switchBoard(@NotNull BoardType boardType) {
        if (this.boardType == boardType) return;
        this.boardType = boardType;
        if (this.currentScoreboard != null) {
            this.viewers.forEach(this.currentScoreboard::removeViewer);
        }
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
        this.currentScoreboard.updateLineContent(scoreType.getName(), value);
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

    @Override
    public void hideBoard() {
        this.viewers.forEach(this.currentScoreboard::removeViewer);
    }

    @Contract(pure = true)
    @Override
    public @NotNull @UnmodifiableView Set<@NotNull Player> getViewers() {
        return Collections.unmodifiableSet(this.viewers);
    }
}
