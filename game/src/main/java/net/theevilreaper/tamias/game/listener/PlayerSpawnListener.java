package net.theevilreaper.tamias.game.listener;

import de.icevizion.aves.util.Broadcaster;
import de.icevizion.xerus.api.phase.GamePhase;
import de.icevizion.xerus.api.phase.LinearPhaseSeries;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class PlayerSpawnListener implements Consumer<PlayerSpawnEvent> {

    private final UUID spawnInstanceID;
    private final LinearPhaseSeries<GamePhase> phaseSeries;

    public PlayerSpawnListener(@NotNull UUID spawnInstanceID, @NotNull LinearPhaseSeries<GamePhase> phaseSeries) {
        this.spawnInstanceID = spawnInstanceID;
        this.phaseSeries = phaseSeries;
    }

    @Override
    public void accept(@NotNull PlayerSpawnEvent event) {
        var player = event.getPlayer();
        player.setDisplayName(Component.text(player.getUsername()));

        var phase = phaseSeries.getCurrentPhase();

        if (phase instanceof LobbyPhase lobbyPhase && player.getInstance().getUniqueId().equals(spawnInstanceID)) {
            Broadcaster.broadcast(GameMessages.getJoinMessage(player));
            lobbyPhase.updatePlayerValues(player);
            return;
        }
    }
}
