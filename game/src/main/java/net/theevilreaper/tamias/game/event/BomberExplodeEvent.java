package net.theevilreaper.tamias.game.event;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * The {@link BomberExplodeEvent} is called when a bomber explodes after the {@link net.theevilreaper.tamias.game.stamina.ExplodeBar} timer is over.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see net.theevilreaper.tamias.game.stamina.ExplodeBar
 * @since 1.0.0
 */
public final class BomberExplodeEvent implements PlayerEvent {

    private final Player player;
    private final Vec vec;

    /**
     * Creates a new instance of the {@link BomberExplodeEvent}.
     *
     * @param player the player who triggered the event
     * @param vec    the position
     */
    public BomberExplodeEvent(Player player, Vec vec) {
        this.player = player;
        this.vec = vec;
    }

    /**
     * Returns the position where the bomber exploded.
     *
     * @return the position
     */
    public Vec getPosition() {
        return this.vec;
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
