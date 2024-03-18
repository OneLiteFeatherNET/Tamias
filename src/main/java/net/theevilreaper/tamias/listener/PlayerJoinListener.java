package net.theevilreaper.tamias.listener;

import de.icevizion.xerus.api.phase.GamePhase;
import de.icevizion.xerus.api.phase.LinearPhaseSeries;
import de.icevizion.xerus.api.phase.Phase;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.theevilreaper.tamias.phase.LobbyPhase;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public class PlayerJoinListener implements Consumer<AsyncPlayerConfigurationEvent> {

    private final LinearPhaseSeries<GamePhase> phaseSeries;

    public PlayerJoinListener(@NotNull LinearPhaseSeries<GamePhase> phaseSeries) {
        this.phaseSeries = phaseSeries;
    }

    @Override
    public void accept(@NotNull AsyncPlayerConfigurationEvent event) {
        Phase phase = phaseSeries.getCurrentPhase();

        if (phase == null) return;

        if (phase instanceof LobbyPhase lobbyPhase) {
            lobbyPhase.checkStartCondition();
        }
    }
}
