package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.area.placement.TntGroup;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.TNT_DROP_HEIGHT;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.TNT_POP_DELAY;

/**
 * The default {@link TntAnimation}: the center of a group falls down from above and the members pop up around it
 * once it has landed.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FallingTntAnimation implements TntAnimation {

    /**
     * The safety limit of the fall in ticks. An entity which never reaches the ground still lands after this many ticks,
     * so a run can never hang.
     */
    static final int MAX_FALL_TICKS = 40;
    private static final float LANDING_VOLUME = 0.6f;
    private static final float LANDING_MIN_PITCH = 0.7f;
    private static final float LANDING_MAX_PITCH = 0.95f;
    private static final Sound POP_SOUND = Sound.sound(SoundEvent.ENTITY_CHICKEN_EGG, Sound.Source.BLOCK, 0.8f, 1.2f);

    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        dropGroup(group, context);
    }

    /**
     * Lets the center of the group fall down with the server physics. The TNT lands once the entity has arrived,
     * the members pop up one after another.
     *
     * @param group   the group to drop
     * @param context the context of the run
     */
    private static void dropGroup(TntGroup group, TntAnimationContext context) {
        Entity tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);

        // The falling block is rendered around its entity position, so the entity is spawned in the middle of the block
        Point entityPos = group.center().add(0.5, TNT_DROP_HEIGHT, 0.5);
        tntEntity.setInstance(context.instance(), entityPos);
        context.track(tntEntity);
        context.instance().sendGroupedPacket(new ParticlePacket(Particle.CLOUD, entityPos, Vec.ZERO, 0f, 5));

        awaitArrival(group, tntEntity, context, 1);
    }

    /**
     * Checks once per tick if the falling entity has arrived. The entity has arrived when it stands on the ground or
     * when it reached the height of the block position. After {@link #MAX_FALL_TICKS} ticks the TNT lands anyway.
     *
     * @param group   the group which falls
     * @param entity  the falling entity of the center
     * @param context the context of the run
     * @param tick    the tick of the fall which is checked next, starting with 1
     */
    private static void awaitArrival(TntGroup group, Entity entity, TntAnimationContext context, int tick) {
        context.schedule(1, () -> {
            boolean arrived = entity.isOnGround() || entity.getPosition().y() <= group.center().y();
            if (!arrived && tick < MAX_FALL_TICKS) {
                awaitArrival(group, entity, context, tick + 1);
                return;
            }
            land(group, entity, context);
        });
    }

    /**
     * Places the TNT of the center, removes the falling entity in the same tick and lets the members pop up.
     *
     * @param group   the group which landed
     * @param entity  the falling entity of the center
     * @param context the context of the run
     */
    private static void land(TntGroup group, Entity entity, TntAnimationContext context) {
        context.land(group.center());
        context.release(entity);
        Point center = group.center().add(0.5);
        context.instance().playSound(landingSound(), center.x(), center.y(), center.z());

        List<Vec> members = group.members();
        for (int i = 0; i < members.size(); i++) {
            Vec member = members.get(i);
            context.schedule((i + 1) * TNT_POP_DELAY, () -> popUp(member, context));
        }
    }

    /**
     * Creates the soft plop which is played when a TNT block lands.
     * The pitch varies a bit, so several blocks which land at the same time don't merge into one sound.
     *
     * @return the sound for the landing
     */
    static Sound landingSound() {
        float pitch = ThreadLocalRandom.current().nextFloat(LANDING_MIN_PITCH, LANDING_MAX_PITCH);
        return Sound.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.BLOCK, LANDING_VOLUME, pitch);
    }

    private static void popUp(Vec position, TntAnimationContext context) {
        context.land(position);
        Point center = position.add(0.5, 0.5, 0.5);
        context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        context.instance().playSound(POP_SOUND, center.x(), center.y(), center.z());
    }
}
