package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.area.placement.TntGroup;

import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link TntAnimation} in which the TNT races like a light cycle from the middle of the game area across the ground
 * to its position. The racer drives along the cell lines only, it never cuts a corner and it drives around blocks
 * which are in its way. Every cell it passes leaves a short glowing trail behind, which fades after a while.
 * The center of a group is launched at once, the members follow one after another.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TronTntAnimation implements TntAnimation {

    /**
     * The delay in ticks between the launch of the center and the launch of each member.
     */
    static final int LAUNCH_DELAY = 3;
    /**
     * The distance in blocks a racer drives per tick.
     */
    static final double RACE_SPEED = 1.2;
    /**
     * The number of ticks a trail part stays before it is removed.
     */
    static final int TRAIL_LIFETIME = 24;
    /**
     * The scale of the racer, the racer is centred on its cell.
     */
    static final double RACER_SCALE = 0.8;
    /**
     * The height in blocks of a trail part, the trail lies flat on its cell.
     */
    static final double TRAIL_THICKNESS = 0.08;
    /**
     * The number of ticks the client interpolates the position of the racer over, it smooths the teleports per tick.
     */
    static final int RACER_INTERPOLATION_TICKS = 1;
    /**
     * The block which is shown as the trail, its light colour reads as a light trail.
     */
    static final Block TRAIL_BLOCK = Block.LIGHT_BLUE_STAINED_GLASS;
    /**
     * The block light and the sky light of the trail, the trail glows at full brightness.
     */
    static final int MAX_LIGHT = 15;
    private static final float LAUNCH_VOLUME = 0.3f;
    private static final float LAUNCH_PITCH = 1.5f;
    private static final double EPSILON = 1e-9;

    private Vec start;

    /**
     * Computes the start cell of the racers: the cell of the average x and z of all positions, on the height of the
     * first group.
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
        double y = groups.getFirst().center().y();
        Vec average = GridRoute.cell(sumX / allPositions.size(), y, sumZ / allPositions.size());
        this.start = GridRoute.nearestPassable(average, cell -> GridRoute.isPassable(context.instance(), cell),
                GridRoute.MAX_SPAWN_RADIUS);
    }

    /**
     * Launches the center of the group at once and each member after its delay. The launch of a center plays a low
     * sound.
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
     * Returns the position of a racer after it moved towards the next cell by at most the given distance. The racer
     * never overshoots the next cell.
     *
     * @param current  the position of the racer
     * @param nextCell the cell the racer drives to
     * @param speed    the maximal distance in blocks to move
     * @return the new position, the next cell itself once it is reached
     */
    static Vec gridStep(Vec current, Vec nextCell, double speed) {
        double distance = current.distance(nextCell);
        if (distance <= speed) return nextCell;
        double factor = speed / distance;
        return new Vec(
                current.x() + (nextCell.x() - current.x()) * factor,
                current.y() + (nextCell.y() - current.y()) * factor,
                current.z() + (nextCell.z() - current.z()) * factor
        );
    }

    /**
     * Starts a racer from the start cell towards the target. Without a route to the target the TNT is placed at once,
     * without a racer.
     *
     * @param target the position of the TNT
     * @param center whether the target is the center of a group, only centers play the launch sound
     * @param context the context of the run
     */
    private void launch(Vec target, boolean center, TntAnimationContext context) {
        Instance instance = context.instance();
        if (center) {
            Point launchCentre = centre(this.start);
            instance.playSound(Sound.sound(SoundEvent.BLOCK_BEACON_ACTIVATE, Sound.Source.BLOCK, LAUNCH_VOLUME,
                    LAUNCH_PITCH), launchCentre.x(), launchCentre.y(), launchCentre.z());
        }

        List<Vec> route = GridRoute.find(this.start, target, cell -> GridRoute.isPassable(instance, cell),
                GridRoute.MAX_ROUTE_NODES);
        if (route == null || route.isEmpty()) {
            placeAt(target, context);
            return;
        }

        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.TNT);
            meta.setScale(new Vec(RACER_SCALE));
            meta.setTranslation(new Vec((1 - RACER_SCALE) / 2));
            meta.setPosRotInterpolationDuration(RACER_INTERPOLATION_TICKS);
        });
        entity.setInstance(instance, this.start);
        context.track(entity);

        Racer racer = new Racer(entity, target, route, this.start);
        context.schedule(1, () -> step(racer, context));
    }

    /**
     * Drives the racer for one tick, up to {@link #RACE_SPEED} blocks along its route. Every cell which is reached is
     * checked for the next one, and a trail part is left behind it. The racer arrives once it reached its target.
     *
     * @param racer   the racer to move
     * @param context the context of the run
     */
    private void step(Racer racer, TntAnimationContext context) {
        double budget = RACE_SPEED;
        while (budget > EPSILON) {
            Vec next = racer.route.get(racer.index);
            double move = Math.min(budget, racer.position.distance(next));
            racer.position = gridStep(racer.position, next, move);
            budget -= move;
            if (!racer.position.equals(next)) break;

            racer.index++;
            if (racer.index == racer.route.size()) {
                arrive(racer, context);
                return;
            }
            trail(next, context);
            if (!isNextCellOpen(racer, context)) {
                arrive(racer, context);
                return;
            }
        }
        racer.entity.teleport(racer.position.asPos());
        context.schedule(1, () -> step(racer, context));
    }

    /**
     * Checks that the next cell of the route is still passable. A blocked cell is replanned once from the cell the
     * racer stands on.
     *
     * @param racer   the racer which reached a cell
     * @param context the context of the run
     * @return false if the racer has no way left to its target
     */
    private boolean isNextCellOpen(Racer racer, TntAnimationContext context) {
        Instance instance = context.instance();
        Predicate<Vec> passable = cell -> GridRoute.isPassable(instance, cell);
        boolean isTarget = racer.index == racer.route.size() - 1;
        if (isTarget || passable.test(racer.route.get(racer.index))) return true;

        List<Vec> route = GridRoute.find(racer.position, racer.target, passable, GridRoute.MAX_ROUTE_NODES);
        if (route == null || route.isEmpty()) return false;
        racer.route = route;
        racer.index = 0;
        return true;
    }

    /**
     * Places the TNT of the racer and removes the racer.
     *
     * @param racer   the racer which arrived
     * @param context the context of the run
     */
    private static void arrive(Racer racer, TntAnimationContext context) {
        placeAt(racer.target, context);
        context.release(racer.entity);
    }

    /**
     * Places the TNT on the target and plays the effects of the landing.
     *
     * @param target  the position which is placed
     * @param context the context of the run
     */
    private static void placeAt(Vec target, TntAnimationContext context) {
        context.land(target);
        Instance instance = context.instance();
        Point center = target.add(0.5, 0.5, 0.5);
        instance.sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        instance.playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
    }

    /**
     * Leaves a flat trail part on the cell which the racer passed. The part is removed after {@link #TRAIL_LIFETIME}.
     *
     * @param cell    the cell which was passed
     * @param context the context of the run
     */
    private static void trail(Vec cell, TntAnimationContext context) {
        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(TRAIL_BLOCK);
            meta.setScale(new Vec(1, TRAIL_THICKNESS, 1));
            meta.setBrightness(MAX_LIGHT, MAX_LIGHT);
        });
        entity.setInstance(context.instance(), cell);
        context.track(entity);
        context.schedule(TRAIL_LIFETIME, () -> context.release(entity));
    }

    private static Point centre(Point cell) {
        return new Vec(cell.x() + 0.5, cell.y(), cell.z() + 0.5);
    }

    /**
     * The state of one racer: its entity, its target, the cells it still has to drive over and its position.
     */
    private static final class Racer {

        private final Entity entity;
        private final Vec target;
        private List<Vec> route;
        private int index;
        private Vec position;

        private Racer(Entity entity, Vec target, List<Vec> route, Vec start) {
            this.entity = entity;
            this.target = target;
            this.route = route;
            this.position = start;
        }
    }
}
