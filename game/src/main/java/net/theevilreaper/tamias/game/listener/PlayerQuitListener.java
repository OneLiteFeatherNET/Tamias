package net.theevilreaper.tamias.game.listener;

import de.icevizion.aves.util.Broadcaster;
import de.icevizion.xerus.api.phase.Phase;
import de.icevizion.xerus.api.team.Team;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.PlayingPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The listener implementation handles the {@link PlayerDisconnectEvent} call, when a player disconnects.
 * This is required to perform operations on different states of the game.
 * As example the quit logic in the {@link LobbyPhase} is different compared to the {@link PlayingPhase}
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class PlayerQuitListener implements Consumer<PlayerDisconnectEvent> {

    private final Supplier<Phase> phaseSupplier;
    private final Function<Integer, Team> teamFunction;

    public PlayerQuitListener(@NotNull Supplier<Phase> phaseSupplier, @NotNull Function<Integer, Team> teamFunction) {
        this.phaseSupplier = phaseSupplier;
        this.teamFunction = teamFunction;
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
     * @param playingPhase the phase reference
     * @param player the involved player
     */
    private void handleGameQuit(@NotNull PlayingPhase playingPhase, @NotNull Player player) {
        if (!player.hasTag(Tags.TEAM_ID)) return;

        int teamId = player.getTag(Tags.TEAM_ID);
        Team team = this.teamFunction.apply(teamId);

        if (team == null) return;
        team.removePlayer(player);
    }

    /**
     * Handles the quit logic for the {@link LobbyPhase}.
     * @param lobbyPhase the reference from the phase
     * @param player the player which is involved
     */
    private void handleLobbyQuit(@NotNull LobbyPhase lobbyPhase, @NotNull Player player) {
        lobbyPhase.checkStopCondition();
        Broadcaster.broadcast(GameMessages.getLeaveMessage(player));
    }

    /**
     *
     * @param player
     */
    private void handleGeneralQuit(@NotNull Player player) {

    }
}
