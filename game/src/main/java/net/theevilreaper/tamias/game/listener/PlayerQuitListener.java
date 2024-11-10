package net.theevilreaper.tamias.game.listener;

import de.icevizion.aves.util.Broadcaster;
import de.icevizion.xerus.api.phase.GamePhase;
import de.icevizion.xerus.api.phase.LinearPhaseSeries;
import de.icevizion.xerus.api.phase.Phase;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public class PlayerQuitListener implements Consumer<PlayerDisconnectEvent> {

    private final LinearPhaseSeries<GamePhase> phaseSeries;

    public PlayerQuitListener(@NotNull LinearPhaseSeries<GamePhase> phaseSeries) {
        this.phaseSeries = phaseSeries;
    }

    @Override
    public void accept(@NotNull PlayerDisconnectEvent event) {
        Phase phase = phaseSeries.getCurrentPhase();

        if (phase == null) return;

        if (phase instanceof LobbyPhase lobbyPhase) {
            lobbyPhase.checkStopCondition();
            Broadcaster.broadcast(GameMessages.getLeaveMessage(event.getPlayer()));
        }
    }
}
