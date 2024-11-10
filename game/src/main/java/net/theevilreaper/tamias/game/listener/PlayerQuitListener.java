package net.theevilreaper.tamias.game.listener;

import de.icevizion.aves.util.Broadcaster;
import de.icevizion.xerus.api.phase.GamePhase;
import de.icevizion.xerus.api.phase.Phase;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public class PlayerQuitListener implements Consumer<PlayerDisconnectEvent> {

    private final Supplier<Phase> phaseSupplier;

    public PlayerQuitListener(@NotNull Supplier<Phase> phaseSupplier) {
        this.phaseSupplier = phaseSupplier;
    }

    @Override
    public void accept(@NotNull PlayerDisconnectEvent event) {
        Phase phase = phaseSupplier.get();

        if (phase == null) return;

        switch (phase) {
            case LobbyPhase lobbyPhase -> handleLobbyQuit(lobbyPhase, event.getPlayer());
            case GamePhase gamePhase -> handleGameQuit(gamePhase, event.getPlayer());
            default -> throw new IllegalStateException("Unexpected value: " + phase);
        }

        if (phase instanceof LobbyPhase lobbyPhase) {
            lobbyPhase.checkStopCondition();
            Broadcaster.broadcast(GameMessages.getLeaveMessage(event.getPlayer()));
        }
    }

    private void handleGameQuit(GamePhase gamePhase, @NotNull Player player) {
    }

    private void handleLobbyQuit(@NotNull LobbyPhase lobbyPhase, @NotNull Player player) {
        lobbyPhase.checkStopCondition();
        Broadcaster.broadcast(GameMessages.getLeaveMessage(player));
    }
}
