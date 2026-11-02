package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * A {@link TntAnimation} in which endermen teleport in to place the TNT. Every position of a group gets an enderman
 * which appears on a passable cell close to the position, looks at it and swings its arm once the TNT is placed.
 * The enderman teleports away again a moment later. The endermen are plain entities without any AI, so they only
 * show up where the animation puts them.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class EndermanTntAnimation implements TntAnimation {

    /**
     * The delay in ticks between the appearance of two positions of the same group.
     */
    static final int TELEPORT_STAGGER = 2;
    /**
     * The maximum random delay in ticks which is added to the stagger of each position.
     */
    static final int RANDOM_DELAY = 6;
    /**
     * The radius in blocks around a position in which an enderman may appear.
     */
    static final int APPEAR_RADIUS = 3;
    /**
     * The number of random cells which are picked before the position itself is used as the spawn cell.
     */
    static final int RANDOM_TRIES = 8;
    /**
     * The delay in ticks between the appearance of an enderman and the placement of its TNT.
     */
    static final int PLACE_DELAY = 6;
    /**
     * The delay in ticks between the placement of the TNT and the teleport away of the enderman.
     */
    static final int LEAVE_DELAY = 5;
    private static final int PARTICLE_COUNT = 12;
    private static final Sound TELEPORT_SOUND = Sound.sound(SoundEvent.ENTITY_ENDERMAN_TELEPORT, Sound.Source.HOSTILE,
            0.3f, 1f);

    private final RandomGenerator random;

    /**
     * Creates the animation with the {@link ThreadLocalRandom} as random source.
     */
    public EndermanTntAnimation() {
        this(ThreadLocalRandom.current());
    }

    /**
     * Creates the animation with the given random source, the delays and the spawn cells use it.
     *
     * @param random the random source of the run
     */
    public EndermanTntAnimation(RandomGenerator random) {
        this.random = random;
    }

    /**
     * Schedules the appearance of every position of the group. The delay of a position is its index times the
     * {@link #TELEPORT_STAGGER} plus a random delay, the delay is at least one tick because the scheduler rejects zero.
     *
     * @param group   the group whose ground is ready
     * @param context the context of the run
     */
    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        List<Vec> positions = group.positions();
        for (int i = 0; i < positions.size(); i++) {
            Vec position = positions.get(i);
            int delay = i * TELEPORT_STAGGER + this.random.nextInt(RANDOM_DELAY + 1);
            context.schedule(Math.max(1, delay), () -> appear(position, context));
        }
    }

    /**
     * Spawns an enderman on a passable cell near the position, faces the position and schedules the placement of the
     * TNT. The enderman is tracked, so a stop removes it.
     *
     * @param position the position of the TNT
     * @param context  the context of the run
     */
    private void appear(Vec position, TntAnimationContext context) {
        Instance instance = context.instance();
        Vec spawnCell = spawnCell(position, context);
        Vec spawn = centre(spawnCell);

        Entity enderman = new Entity(EntityType.ENDERMAN);
        enderman.setNoGravity(true);
        enderman.setInstance(instance, spawn);
        context.track(enderman);
        enderman.lookAt(centre(position));

        Point effect = spawn.add(0, 1, 0);
        instance.sendGroupedPacket(new ParticlePacket(Particle.PORTAL, effect, new Vec(0.5, 1, 0.5), 0.1f,
                PARTICLE_COUNT));
        instance.playSound(TELEPORT_SOUND, effect.x(), effect.y(), effect.z());
        context.schedule(PLACE_DELAY, () -> place(position, enderman, context));
    }

    /**
     * Picks the cell where the enderman appears. A random cell within {@link #APPEAR_RADIUS} on the height of the
     * position is picked up to {@link #RANDOM_TRIES} times, the first free one with placed ground is used. Without
     * one a free cell inside the game area is tried the same way, and without that the position itself is used.
     *
     * @param position the position of the TNT
     * @param context  the context of the run
     * @return the cell of the spawn
     */
    private Vec spawnCell(Vec position, TntAnimationContext context) {
        Vec origin = GridRoute.cellOf(position);
        Vec onGround = randomCell(origin, GridRoute.onGround(context));
        if (onGround != null) return onGround;
        Vec inArea = randomCell(origin, GridRoute.inArea(context));
        return inArea != null ? inArea : origin;
    }

    private @Nullable Vec randomCell(Vec origin, Predicate<Vec> allowed) {
        for (int i = 0; i < RANDOM_TRIES; i++) {
            int dx = this.random.nextInt(-APPEAR_RADIUS, APPEAR_RADIUS + 1);
            int dz = this.random.nextInt(-APPEAR_RADIUS, APPEAR_RADIUS + 1);
            Vec cell = origin.add(dx, 0, dz);
            if (allowed.test(cell)) return cell;
        }
        return null;
    }

    /**
     * Swings the arm of the enderman, places the TNT and plays the effects of the landing. The enderman leaves after
     * {@link #LEAVE_DELAY} ticks.
     *
     * @param position the position which is placed
     * @param enderman the enderman which places the TNT
     * @param context  the context of the run
     */
    private static void place(Vec position, Entity enderman, TntAnimationContext context) {
        Instance instance = context.instance();
        instance.sendGroupedPacket(new EntityAnimationPacket(enderman.getEntityId(),
                EntityAnimationPacket.Animation.SWING_MAIN_ARM));
        context.land(position);

        Point center = position.add(0.5, 0.5, 0.5);
        instance.sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        instance.playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
        context.schedule(LEAVE_DELAY, () -> leave(enderman, context));
    }

    /**
     * Lets the enderman teleport away and removes it.
     *
     * @param enderman the enderman which leaves
     * @param context  the context of the run
     */
    private static void leave(Entity enderman, TntAnimationContext context) {
        Instance instance = context.instance();
        Point effect = enderman.getPosition().add(0, 1, 0);
        instance.sendGroupedPacket(new ParticlePacket(Particle.PORTAL, effect, new Vec(0.5, 1, 0.5), 0.1f,
                PARTICLE_COUNT));
        instance.playSound(TELEPORT_SOUND, effect.x(), effect.y(), effect.z());
        context.release(enderman);
    }

    /**
     * Returns the centre of the cell at the height of the cell.
     *
     * @param cell the cell
     * @return the centre of the cell
     */
    private static Vec centre(Point cell) {
        return new Vec(cell.x() + 0.5, cell.y(), cell.z() + 0.5);
    }
}
