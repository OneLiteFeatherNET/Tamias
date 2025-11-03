package net.theevilreaper.tamias.game.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * The event is called when player switches the role to bomber.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RoleToBomberChangeEvent implements PlayerEvent {

    private final Player player;

    /**
     * Creates a new instance of the {@link RoleToBomberChangeEvent}.
     *
     * @param player the player who should change the role
     */
    public RoleToBomberChangeEvent(Player player) {
        this.player = player;
    }

    /**
     * Returns the player who triggered the event.
     *
     * @return the player
     */
    @Override
    public Player getPlayer() {
        return this.player;
    }
}
