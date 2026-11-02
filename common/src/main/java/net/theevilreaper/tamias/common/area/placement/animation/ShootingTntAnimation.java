package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.area.placement.TntGroup;

import java.util.List;

/**
 * A {@link TntAnimation} in which the TNT is shot from the middle of the game area in an arc onto its position.
 * The center of a group is launched at once, the members follow one after another. The flight follows a parabola
 * whose height depends on the distance, the TNT is placed once it has arrived.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ShootingTntAnimation implements TntAnimation {

    /**
     * The delay in ticks between the launch of the center and the launch of each member.
     */
    static final int LAUNCH_DELAY = 3;
    /**
     * The speed of a shot in blocks per tick, the flight takes about the distance divided by this speed.
     */
    static final double FLIGHT_SPEED = 0.7;
    static final int MIN_FLIGHT_TICKS = 10;
    static final int MAX_FLIGHT_TICKS = 40;
    /**
     * The minimal height in blocks of the arc above the straight line of a shot.
     */
    static final double MIN_ARC = 2.0;
    /**
     * The height of the arc per block of distance, a longer shot flies higher.
     */
    static final double ARC_FACTOR = 0.3;

    private Vec launchPoint;

    /**
     * Computes the launch point in the middle of the group positions, above the first group.
     *
     * @param groups  all groups of the run
     * @param context the context of the run
     */
    @Override
    public void begin(List<TntGroup> groups, TntAnimationContext context) {
        List<Vec> allPositions = groups.stream().flatMap(group -> group.positions().stream()).toList();
        if (allPositions.isEmpty()) return;

        double sumX = 0;
        double sumZ = 0;
        for (Vec position : allPositions) {
            sumX += position.x();
            sumZ += position.z();
        }
        this.launchPoint = new Vec(
                sumX / allPositions.size() + 0.5,
                groups.getFirst().center().y() + 1,
                sumZ / allPositions.size() + 0.5
        );
    }

    /**
     * Launches the center of the group at once and each member after its delay. The group is only played once its
     * ground is ready, so no TNT is shot before the ground is there.
     *
     * @param group   the group to launch
     * @param context the context of the run
     */
    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        launch(group.center(), true, context);
        List<Vec> members = group.members();
        for (int i = 0; i < members.size(); i++) {
            Vec member = members.get(i);
            context.schedule((i + 1) * LAUNCH_DELAY, () -> launch(member, false, context));
        }
    }

    /**
     * Returns the number of ticks a shot over the given distance flies.
     *
     * @param distance the distance between the launch point and the target in blocks
     * @return the flight ticks, clamped to {@link #MIN_FLIGHT_TICKS} and {@link #MAX_FLIGHT_TICKS}
     */
    static int flightTicks(double distance) {
        long ticks = Math.round(distance / FLIGHT_SPEED);
        return (int) Math.clamp(ticks, MIN_FLIGHT_TICKS, MAX_FLIGHT_TICKS);
    }

    /**
     * Returns the height of the arc of a shot over the given distance.
     *
     * @param distance the distance between the launch point and the target in blocks
     * @return the arc height in blocks
     */
    static double arcHeight(double distance) {
        return Math.max(MIN_ARC, distance * ARC_FACTOR);
    }

    /**
     * Returns the position of the shot at the given tick. The x and z coordinates move linearly from the launch point
     * to the target, the height is linear between both plus a parabola which peaks at {@code height} in the middle.
     *
     * @param launch       the launch point of the shot
     * @param target       the target centre of the shot
     * @param height       the height of the arc above the straight line in the middle of the flight
     * @param tick         the tick of the flight, 0 is the launch and {@code flightTicks} is the arrival
     * @param flightTicks  the number of ticks of the whole flight
     * @return the position of the shot
     */
    static Vec pointOnPath(Point launch, Point target, double height, int tick, int flightTicks) {
        double t = (double) tick / flightTicks;
        double x = launch.x() + (target.x() - launch.x()) * t;
        double y = launch.y() + (target.y() - launch.y()) * t + 4 * height * t * (1 - t);
        double z = launch.z() + (target.z() - launch.z()) * t;
        return new Vec(x, y, z);
    }

    /**
     * Spawns a falling TNT at the launch point and lets it fly towards the target.
     *
     * @param target  the block position the TNT lands on
     * @param center  whether the target is the center of a group, only centers play the launch sound
     * @param context the context of the run
     */
    private void launch(Vec target, boolean center, TntAnimationContext context) {
        Instance instance = context.instance();
        Vec launch = this.launchPoint;
        Vec targetCentre = new Vec(target.x() + 0.5, target.y(), target.z() + 0.5);

        if (center) {
            Sound launchSound = Sound.sound(SoundEvent.ENTITY_FIREWORK_ROCKET_LAUNCH, Sound.Source.BLOCK, 0.7f, 1f);
            instance.playSound(launchSound, launch.x(), launch.y(), launch.z());
        }
        instance.sendGroupedPacket(new ParticlePacket(Particle.SMOKE, launch, new Vec(0.3), 0.01f, 6));

        Entity entity = new Entity(EntityType.FALLING_BLOCK);
        ((FallingBlockMeta) entity.getEntityMeta()).setBlock(Block.TNT);
        entity.setNoGravity(true);
        entity.setInstance(instance, launch);
        context.track(entity);

        double distance = launch.distance(targetCentre);
        Shot shot = new Shot(entity, target, targetCentre, launch, arcHeight(distance), flightTicks(distance));
        context.schedule(1, () -> fly(shot, context));
    }

    /**
     * Moves the shot to its position of the current tick. The shot lands once it reached the flight ticks.
     *
     * @param shot    the shot to move
     * @param context the context of the run
     */
    private static void fly(Shot shot, TntAnimationContext context) {
        shot.tick++;
        if (shot.tick < shot.flightTicks) {
            Vec position = pointOnPath(shot.launch, shot.targetCentre, shot.height, shot.tick, shot.flightTicks);
            shot.entity.teleport(new Pos(position));
            context.schedule(1, () -> fly(shot, context));
            return;
        }
        arrive(shot, context);
    }

    /**
     * Places the TNT on the target, removes the shot and plays the effects of the landing.
     *
     * @param shot    the shot which arrived
     * @param context the context of the run
     */
    private static void arrive(Shot shot, TntAnimationContext context) {
        context.land(shot.target);
        context.release(shot.entity);
        Point center = shot.targetCentre.add(0, 0.5, 0);
        context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        context.instance().playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
    }

    /**
     * One TNT block on its way from the launch point to the target.
     */
    private static final class Shot {

        private final Entity entity;
        private final Vec target;
        private final Vec targetCentre;
        private final Vec launch;
        private final double height;
        private final int flightTicks;
        private int tick;

        private Shot(Entity entity, Vec target, Vec targetCentre, Vec launch, double height, int flightTicks) {
            this.entity = entity;
            this.target = target;
            this.targetCentre = targetCentre;
            this.launch = launch;
            this.height = height;
            this.flightTicks = flightTicks;
        }
    }
}
