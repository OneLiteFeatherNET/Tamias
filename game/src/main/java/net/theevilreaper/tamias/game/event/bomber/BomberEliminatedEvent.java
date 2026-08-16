package net.theevilreaper.tamias.game.event.bomber;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * The {@link BomberEliminatedEvent} is called when a Bomber is hit by a Survivor's
 * gun projectile. Unlike {@link BomberExplodeEvent} (self-detonation), an elimination
 * never converts nearby Survivors to Bomber it only triggers that one player's respawn.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see net.theevilreaper.tamias.game.listener.game.ProjectileEntityListener
 * @since 1.0.0
 */
public final class BomberEliminatedEvent implements PlayerEvent {

    private final Player player;
    private final Vec position;

    /**
     * Creates a new instance of the {@link BomberEliminatedEvent}.
     *
     * @param player   the eliminated player
     * @param position the position where the elimination happened
     */
    public BomberEliminatedEvent(Player player, Vec position) {
        this.player = player;
        this.position = position;
    }

    /**
     * Returns the position where the elimination happened.
     *
     * @return the position
     */
    public Vec getPosition() {
        return this.position;
    }

    /**
     * Returns the player who was eliminated.
     *
     * @return the player
     */
    @Override
    public Player getPlayer() {
        return this.player;
    }
}
