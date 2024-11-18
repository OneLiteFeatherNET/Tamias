package net.theevilreaper.tamias.game.listener;

import de.icevizion.aves.util.Broadcaster;
import de.icevizion.aves.util.functional.PlayerConsumer;
import de.icevizion.xerus.api.phase.Phase;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.PlayerSpawnEvent;
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

public final class PlayerSpawnListener implements Consumer<PlayerSpawnEvent> {

    private final Supplier<Phase> phaseSupplier;
    private final PlayerConsumer spawnConsumer;

    public PlayerSpawnListener(@NotNull Supplier<Phase> phaseSupplier, @NotNull PlayerConsumer spawnConsumer) {
        this.phaseSupplier = phaseSupplier;
        this.spawnConsumer = spawnConsumer;
    }

    @Override
    public void accept(@NotNull PlayerSpawnEvent event) {
        var player = event.getPlayer();
        player.setDisplayName(Component.text(player.getUsername()));

        var phase = phaseSupplier.get();

        if (event.isFirstSpawn() && phase instanceof LobbyPhase lobbyPhase) {
            Broadcaster.broadcast(GameMessages.getJoinMessage(player));
            lobbyPhase.updatePlayerValues(player);
            lobbyPhase.checkStartCondition();
            this.spawnConsumer.accept(player);
            return;
        }
    }
}
