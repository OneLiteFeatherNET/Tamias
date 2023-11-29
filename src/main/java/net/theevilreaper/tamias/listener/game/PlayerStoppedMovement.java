package net.theevilreaper.tamias.listener.game;

import net.minestom.server.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayerStoppedMovement implements Consumer<PlayerMoveEvent> {
    @Override
    public void accept(@NotNull PlayerMoveEvent event) {
        var playerPosition = event.getPlayer().getPosition();
        var newPos = event.getNewPosition();

        if (playerPosition.x() != newPos.x() || playerPosition.z() != newPos.z()) {
            event.getPlayer().teleport(playerPosition);
        }
    }
}
