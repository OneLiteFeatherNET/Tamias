package net.theevilreaper.tamias.common.area.placement.animation;

import net.theevilreaper.tamias.common.area.placement.TntGroup;

import java.util.List;

/**
 * A TntAnimation decides how a TNT block visually reaches its position. The
 * {@link net.theevilreaper.tamias.common.area.placement.TNTPlacement} owns the queue, the ground check and the
 * completion and calls the animation for every group.
 * <p>
 * An instance holds the state of exactly one run, so a new instance is needed for every run.
 * Every position of a group must eventually be passed to {@link TntAnimationContext#land(net.minestom.server.coordinate.Vec)},
 * otherwise the run never completes.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TntAnimation {

    /**
     * Called once when a run starts, before any {@link #play(TntGroup, TntAnimationContext)} call.
     * It receives all groups of the run, so an animation which acts on the whole run can prepare itself here.
     *
     * @param groups  all groups of the run in the order they are dropped
     * @param context the context which the animation uses to schedule, track and land the TNT
     */
    default void begin(List<TntGroup> groups, TntAnimationContext context) {}

    /**
     * Called once for each group as soon as the ground below all of its blocks is placed.
     *
     * @param group   the group to animate
     * @param context the context which the animation uses to schedule, track and land the TNT
     */
    void play(TntGroup group, TntAnimationContext context);
}
