package net.theevilreaper.tamias.game.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The event is called when player switches the role to bomber.
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author theEvilReaper
 */
public final class RoleToBomberChangeEvent implements PlayerEvent {

    private final Player player;

    public RoleToBomberChangeEvent(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }
}
