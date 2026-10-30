package net.theevilreaper.tamias.game.event;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * The {@link BomberExplodeEvent} is called when a bomber explodes after the {@link net.theevilreaper.tamias.game.stamina.ExplodeBar} timer is over
 * or when an egg hits the bomber.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see net.theevilreaper.tamias.game.stamina.ExplodeBar
 * @since 1.0.0
 */
public final class BomberExplodeEvent implements PlayerEvent {

    private final Player player;
    private final Vec vec;
    private final boolean shot;

    /**
     * Creates a new instance of the {@link BomberExplodeEvent} for an explosion after the timer is over.
     *
     * @param player the player who triggered the event
     * @param vec    the position
     */
    public BomberExplodeEvent(Player player, Vec vec) {
        this(player, vec, false);
    }

    /**
     * Creates a new instance of the {@link BomberExplodeEvent}.
     *
     * @param player the player who triggered the event
     * @param vec    the position
     * @param shot   true if an egg hit the bomber and caused the explosion
     */
    public BomberExplodeEvent(Player player, Vec vec, boolean shot) {
        this.player = player;
        this.vec = vec;
        this.shot = shot;
    }

    /**
     * Returns if an egg hit the bomber and caused the explosion.
     *
     * @return true if the bomber was shot
     */
    public boolean isShot() {
        return this.shot;
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
