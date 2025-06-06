package net.theevilreaper.tamias.game.listener;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static net.minestom.server.MinecraftServer.getConnectionManager;

/**
 * The listener implementation handles the {@link PlayerDisconnectEvent} call, when a player disconnects.
 * This is required to perform operations on different states of the game.
 * As example the quit logic in the {@link LobbyPhase} is different compared to the {@link PlayingPhase}
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayerQuitListener implements Consumer<PlayerDisconnectEvent> {

    private final Supplier<Phase> phaseSupplier;
    private final IntFunction<Team> teamFunction;
    private final VoidConsumer roundEndCheck;
    private final IntConsumer playerCountUpdater;

    public PlayerQuitListener(
            @NotNull Supplier<Phase> phaseSupplier,
            @NotNull IntFunction<Team> teamFunction,
            @NotNull VoidConsumer roundEndCheck,
            @NotNull IntConsumer playerCountUpdater
    ) {
        this.phaseSupplier = phaseSupplier;
        this.teamFunction = teamFunction;
        this.roundEndCheck = roundEndCheck;
        this.playerCountUpdater = playerCountUpdater;
    }

    @Override
    public void accept(@NotNull PlayerDisconnectEvent event) {
        Phase phase = phaseSupplier.get();

        if (phase == null) return;

        Player player = event.getPlayer();

        switch (phase) {
            case LobbyPhase lobbyPhase -> this.handleLobbyQuit(lobbyPhase, event.getPlayer());
            case PlayingPhase playingPhase -> this.handleGameQuit(playingPhase, player);
            default -> this.handleGeneralQuit(player);
        }
    }

    /**
     * Handles the quit logic for the {@link PlayingPhase}.
     *
     * @param playingPhase the phase reference
     * @param player       the involved player
     */
    private void handleGameQuit(@NotNull PlayingPhase playingPhase, @NotNull Player player) {
        if (!player.hasTag(Tags.TEAM_ID)) return;

        int teamId = player.getTag(Tags.TEAM_ID);
        Team team = this.teamFunction.apply(teamId);

        if (team == null) return;
        team.removePlayer(player);
        this.roundEndCheck.apply();
    }

    /**
     * Handles the quit logic for the {@link LobbyPhase}.
     *
     * @param lobbyPhase the reference from the phase
     * @param player     the player which is involved
     */
    private void handleLobbyQuit(@NotNull LobbyPhase lobbyPhase, @NotNull Player player) {
        lobbyPhase.checkStopCondition();
        Audience.audience(getConnectionManager().getOnlinePlayers()).sendMessage(GameMessages.getLeaveMessage(player));
        this.playerCountUpdater.accept(getConnectionManager().getOnlinePlayerCount());
    }

    /**
     * @param player
     */
    private void handleGeneralQuit(@NotNull Player player) {

    }
}
