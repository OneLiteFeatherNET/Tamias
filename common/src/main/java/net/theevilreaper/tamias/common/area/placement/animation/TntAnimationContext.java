package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;

/**
 * The context of a {@link TntAnimation} run. It is the only way an animation can schedule work, track entities or
 * make a TNT block real.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TntAnimationContext {

    /**
     * Returns the instance where the TNT is placed.
     *
     * @return the instance of the run
     */
    Instance instance();

    /**
     * Runs the action after the given delay. The action is cancelled when the placement stops.
     *
     * @param delayTicks the delay in ticks
     * @param action     the action to run
     */
    void schedule(int delayTicks, Runnable action);

    /**
     * Tracks the entity, it is removed when the placement stops.
     *
     * @param entity the entity to track
     */
    void track(Entity entity);

    /**
     * Removes the entity now and stops tracking it.
     *
     * @param entity the entity to remove
     */
    void release(Entity entity);

    /**
     * Places the real TNT block at the position and creates its respawn ticket.
     * Nothing happens once the placement is stopped.
     *
     * @param position the position where the TNT lands
     */
    void land(Vec position);
}
