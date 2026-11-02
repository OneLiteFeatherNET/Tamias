package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.theevilreaper.tamias.common.area.placement.TntGroup;

import java.util.List;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.TNT_POP_DELAY;

/**
 * A {@link TntAnimation} in which the TNT pops up on the spot: a block display starts very small in the middle of the
 * position, grows past its full size and then settles to it. The first leg scales to {@link #OVERSHOOT_SCALE}, the
 * second leg settles back to the full size. The center appears at once, the members follow one after another.
 * The real TNT block is placed once the display has settled.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GrowingTntAnimation implements TntAnimation {

    /**
     * The delay in ticks until the display starts to grow, the client must know the entity before it interpolates.
     */
    static final int GROW_START_DELAY = 3;
    /**
     * The duration in ticks of the first leg, the display grows from {@link #START_SCALE} to {@link #OVERSHOOT_SCALE}.
     */
    static final int GROW_TICKS = 14;
    /**
     * The duration in ticks of the second leg, the display settles from {@link #OVERSHOOT_SCALE} to the full size.
     */
    static final int SETTLE_TICKS = 6;
    /**
     * The total duration in ticks from the start until the TNT block is placed.
     */
    static final int TOTAL_TICKS = GROW_START_DELAY + GROW_TICKS + SETTLE_TICKS;
    /**
     * The scale of the display when it appears.
     */
    static final double START_SCALE = 0.05;
    /**
     * The scale the display overshoots to before it settles, so the TNT pops past its full size.
     */
    static final double OVERSHOOT_SCALE = 1.3;

    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        grow(group.center(), context);
        List<Vec> members = group.members();
        for (int i = 0; i < members.size(); i++) {
            Vec member = members.get(i);
            context.schedule((i + 1) * TNT_POP_DELAY, () -> grow(member, context));
        }
    }

    /**
     * Spawns a block display at the position which pops up and settles to the full block size, and lands the TNT once
     * it has settled.
     *
     * @param position the block position of the TNT
     * @param context  the context of the run
     */
    private static void grow(Vec position, TntAnimationContext context) {
        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.TNT);
            applyTransformation(meta, START_SCALE, 0);
        });

        entity.setInstance(context.instance(), position);
        context.track(entity);

        context.schedule(GROW_START_DELAY, () -> animateTo(entity, OVERSHOOT_SCALE, GROW_TICKS));
        context.schedule(GROW_START_DELAY + GROW_TICKS, () -> animateTo(entity, 1, SETTLE_TICKS));
        context.schedule(TOTAL_TICKS, () -> {
            context.land(position);
            context.release(entity);
            Point center = position.add(0.5, 0.5, 0.5);
            context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
            context.instance().playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
        });
    }

    /**
     * Interpolates the display to the given scale within one metadata update, so the client receives a single packet
     * for the whole leg.
     *
     * @param entity the display to animate
     * @param scale  the scale to interpolate to
     * @param ticks  the duration of the interpolation in ticks
     */
    private static void animateTo(Entity entity, double scale, int ticks) {
        entity.editEntityMeta(BlockDisplayMeta.class, meta -> applyTransformation(meta, scale, ticks));
    }

    /**
     * Sets the scale, the translation and the interpolation of the display. The translation moves the scaled block so
     * it stays centered in its position, for a scale above 1 it moves the block back.
     *
     * @param meta     the display to change
     * @param scale    the scale of the display
     * @param duration the duration of the interpolation in ticks, 0 for no interpolation
     */
    private static void applyTransformation(BlockDisplayMeta meta, double scale, int duration) {
        meta.setTransformationInterpolationStartDelta(0);
        meta.setTransformationInterpolationDuration(duration);
        meta.setScale(new Vec(scale));
        meta.setTranslation(new Vec((1 - scale) / 2));
    }
}
