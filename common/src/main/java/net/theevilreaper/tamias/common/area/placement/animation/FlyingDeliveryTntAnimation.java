package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * A {@link TntAnimation} in which flying animals deliver the TNT like drones. A few carriers wait above a depot in the
 * middle of the game area. Each carrier takes the nearest open position and hangs a block of TNT below itself on a
 * leash. The carrier flies up to a cruise height, then level to above the position and down to the position. The
 * TNT swings on the leash behind the carrier. Once the TNT is placed the leash is removed and the carrier takes the
 * next open position right away with a new block of TNT. Only when no position is open, it flies back to the depot.
 * The flight stays above the target, so walls are passed and never need a route.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FlyingDeliveryTntAnimation implements TntAnimation {

    /**
     * The number of positions which are served by one carrier, the carrier count grows with the positions.
     */
    static final int POSITIONS_PER_CARRIER = 6;
    /**
     * The fewest carriers which deliver the TNT, fewer are spawned only if the run has fewer positions.
     */
    static final int MIN_CARRIERS = 5;
    /**
     * The most carriers which deliver the TNT.
     */
    static final int MAX_CARRIERS = 16;
    /**
     * The height in blocks of the depot above the first group of the run.
     */
    static final double DEPOT_HEIGHT = 10;
    /**
     * The edge length of the carried TNT block. The entity position of the block is its top centre, so the leash
     * attaches at the top of the block.
     */
    static final double PARCEL_SCALE = 0.7;
    /**
     * The length in blocks of the leash between a carrier and the TNT block below it, measured from the carrier to the
     * top of the block.
     */
    static final double ROPE_LENGTH = 2.0;
    /**
     * The maximum distance in blocks between a carrier and its TNT block, the block is pulled in when it is further away.
     */
    static final double MAX_ROPE = 2.6;
    /**
     * The share of the distance to the hanging point which the TNT block covers per tick. A value below one makes the
     * block trail behind the carrier and swing.
     */
    static final double PARCEL_FOLLOW = 0.35;
    /**
     * The interpolation of the TNT block between two of its positions in ticks, it smooths the movement on the client.
     */
    static final int PARCEL_INTERPOLATION = 2;
    /**
     * The fastest distance in blocks a carrier flies per tick.
     */
    static final double FLY_SPEED = 0.7;
    /**
     * The slowest distance in blocks a carrier flies per tick, it guarantees that every leg arrives.
     */
    static final double MIN_SPEED = 0.15;
    /**
     * The number of ticks a carrier needs to reach its full speed at the start of a leg.
     */
    static final int ACCEL_TICKS = 10;
    /**
     * The distance in blocks before the goal of a leg at which a carrier starts to slow down.
     */
    static final double BRAKE_DISTANCE = 3;
    /**
     * The height in blocks above a target at which a carrier flies level towards the target.
     */
    static final double CRUISE_HEIGHT = 5;
    /**
     * The height in blocks above a target at which a carrier drops its TNT, the TNT block then hangs just above the
     * target.
     */
    static final double LANDING_HEIGHT = ROPE_LENGTH + 0.8;
    /**
     * The number of ticks a carrier waits after its descent, so the swing of the TNT block dies down.
     */
    static final int SETTLE_TICKS = 4;
    /**
     * An ambient sound of the carrier is played every this many landings.
     */
    static final int SOUND_INTERVAL = 3;
    /**
     * The maximum height in blocks of the bobbing of an idle carrier above the depot.
     */
    static final double BOB_AMPLITUDE = 0.3;
    /**
     * How fast an idle carrier bobs, the bobbing is the sine of the tick counter times this factor.
     */
    static final double BOB_FREQUENCY = 0.1;
    private static final Vec PARCEL_TRANSLATION = new Vec(-PARCEL_SCALE / 2, -PARCEL_SCALE, -PARCEL_SCALE / 2);
    private static final float SOUND_VOLUME = 0.4f;
    private static final Map<EntityType, SoundEvent> AMBIENT_SOUNDS = Map.of(
            EntityType.PARROT, SoundEvent.ENTITY_PARROT_AMBIENT,
            EntityType.BAT, SoundEvent.ENTITY_BAT_AMBIENT,
            EntityType.PHANTOM, SoundEvent.ENTITY_PHANTOM_AMBIENT
    );

    private final EntityType carrierType;
    private final List<Vec> openPositions = new ArrayList<>();
    private final List<Carrier> carriers = new ArrayList<>();
    private Vec depot;
    private int totalPositions;
    private int landedCount;
    private boolean finished;

    /**
     * Creates the animation with the type of the carriers, e.g. a parrot, a bat or a phantom.
     *
     * @param carrierType the entity type of the carriers
     */
    public FlyingDeliveryTntAnimation(EntityType carrierType) {
        this.carrierType = carrierType;
    }

    /**
     * Returns the number of carriers for the given number of positions. The count grows with the positions, is kept
     * between {@link #MIN_CARRIERS} and {@link #MAX_CARRIERS} and never exceeds the positions.
     *
     * @param positions the number of positions of the run
     * @return the number of carriers which deliver the TNT
     */
    static int carrierCount(int positions) {
        return Math.min(positions, Math.clamp(positions / POSITIONS_PER_CARRIER, MIN_CARRIERS, MAX_CARRIERS));
    }

    /**
     * Computes the depot above the middle of all positions and spawns the carriers there. The carriers are spawned
     * before any position is ready, so they bob at the depot until the ground of a group is placed.
     *
     * @param groups  all groups of the run
     * @param context the context of the run
     */
    @Override
    public void begin(List<TntGroup> groups, TntAnimationContext context) {
        List<Vec> allPositions = groups.stream().flatMap(group -> group.positions().stream()).toList();
        this.totalPositions = allPositions.size();
        if (allPositions.isEmpty()) return;

        double sumX = 0;
        double sumZ = 0;
        for (Vec position : allPositions) {
            sumX += position.x();
            sumZ += position.z();
        }
        double y = groups.getFirst().center().y() + DEPOT_HEIGHT;
        this.depot = new Vec(sumX / allPositions.size() + 0.5, y, sumZ / allPositions.size() + 0.5);

        int count = carrierCount(allPositions.size());
        for (int i = 0; i < count; i++) {
            Entity entity = new Entity(this.carrierType);
            entity.setNoGravity(true);
            entity.setInstance(context.instance(), this.depot);
            context.track(entity);

            Carrier carrier = new Carrier(entity, this.depot);
            this.carriers.add(carrier);
            context.schedule(1, () -> step(carrier, context));
        }
    }

    /**
     * Adds the positions of the group to the open work, a carrier only goes to a position once it is added here.
     *
     * @param group   the group whose ground is ready
     * @param context the context of the run
     */
    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        this.openPositions.addAll(group.positions());
    }

    /**
     * Moves one carrier by one tick, according to its phase. The TNT block of a carrier follows it on the leash. A
     * carrier which is not finished schedules its next tick.
     *
     * @param carrier the carrier to move
     * @param context the context of the run
     */
    private void step(Carrier carrier, TntAnimationContext context) {
        if (this.finished) return;
        carrier.ticks++;
        switch (carrier.phase) {
            case IDLE -> idle(carrier, context);
            case DELIVER -> {
                if (flyRoute(carrier)) {
                    carrier.phase = Phase.SETTLE;
                    carrier.settleTicks = 0;
                }
            }
            case SETTLE -> {
                carrier.settleTicks++;
                if (carrier.settleTicks >= SETTLE_TICKS) {
                    land(carrier, context);
                }
            }
            case RETURN -> {
                if (flyRoute(carrier)) {
                    carrier.phase = Phase.IDLE;
                }
            }
        }

        if (carrier.parcel != null) {
            swingParcel(carrier);
        }
        if (!this.finished) {
            context.schedule(1, () -> step(carrier, context));
        }
    }

    /**
     * Takes the nearest open position and picks up the TNT for it. Without an open position the carrier bobs above
     * the depot.
     *
     * @param carrier the idle carrier at the depot
     * @param context the context of the run
     */
    private void idle(Carrier carrier, TntAnimationContext context) {
        Vec target = takeNearestOpen(carrier.position);
        if (target != null) {
            pickUp(carrier, target, context);
            return;
        }

        double bob = Math.sin(carrier.ticks * BOB_FREQUENCY) * BOB_AMPLITUDE;
        Vec hover = this.depot.add(0, bob, 0);
        carrier.position = new Pos(hover, carrier.position.yaw(), carrier.position.pitch());
        carrier.entity.teleport(carrier.position);
    }

    /**
     * Spawns the block of TNT on a leash below the carrier and sets the carrier on its way to the target. The route
     * goes up from the depot to the cruise height, level to above the target and down to the landing point.
     *
     * @param carrier the carrier which takes the TNT
     * @param target  the block position the TNT is delivered to
     * @param context the context of the run
     */
    private void pickUp(Carrier carrier, Vec target, TntAnimationContext context) {
        Entity parcel = new Entity(EntityType.BLOCK_DISPLAY);
        parcel.setNoGravity(true);
        parcel.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.TNT);
            meta.setScale(new Vec(PARCEL_SCALE));
            meta.setTranslation(PARCEL_TRANSLATION);
            meta.setPosRotInterpolationDuration(PARCEL_INTERPOLATION);
        });
        Vec hang = hangPoint(Vec.fromPoint(carrier.position));
        parcel.setInstance(context.instance(), new Pos(hang));
        context.track(parcel);

        carrier.parcel = parcel;
        carrier.parcelPosition = hang;
        carrier.entity.setLeashHolder(parcel);
        carrier.target = target;

        Vec cruise = cruisePoint(target);
        carrier.route.clear();
        // The carrier first climbs straight up from where it is, at the depot or above its last position
        carrier.route.add(new Vec(carrier.position.x(), cruise.y(), carrier.position.z()));
        carrier.route.add(cruise);
        carrier.route.add(landingPoint(target));
        carrier.legTicks = 0;
        carrier.phase = Phase.DELIVER;
    }

    /**
     * Places the TNT of the carrier's target, removes the leash and the block of TNT and plays the effects of the
     * landing. The last landing finishes the run, every other carrier flies back to the depot on the route above the
     * target.
     *
     * @param carrier the carrier which reached the landing point
     * @param context the context of the run
     */
    private void land(Carrier carrier, TntAnimationContext context) {
        Vec target = carrier.target;
        Entity parcel = carrier.parcel;
        carrier.target = null;
        carrier.parcel = null;
        carrier.parcelPosition = null;

        context.land(target);
        carrier.entity.setLeashHolder(null);
        context.release(parcel);
        this.landedCount++;

        Instance instance = context.instance();
        Point center = target.add(0.5);
        instance.sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        instance.playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
        SoundEvent ambient = AMBIENT_SOUNDS.get(this.carrierType);
        if (ambient != null && this.landedCount % SOUND_INTERVAL == 0) {
            Point carrierPosition = carrier.position;
            Sound ambientSound = Sound.sound(ambient, Sound.Source.NEUTRAL, SOUND_VOLUME, 1f);
            instance.playSound(ambientSound, carrierPosition.x(), carrierPosition.y(), carrierPosition.z());
        }

        if (this.landedCount == this.totalPositions) {
            finish(context);
            return;
        }

        // The carrier takes the next position right away instead of flying back to the depot first
        Vec next = takeNearestOpen(target);
        if (next != null) {
            pickUp(carrier, next, context);
            return;
        }

        Vec cruise = cruisePoint(target);
        carrier.route.clear();
        carrier.route.add(cruise);
        carrier.route.add(new Vec(this.depot.x(), cruise.y(), this.depot.z()));
        carrier.route.add(this.depot);
        carrier.legTicks = 0;
        carrier.phase = Phase.RETURN;
    }

    /**
     * Ends the run: every carrier has its leash removed, makes a last poof and is removed. All positions have landed,
     * so no carrier carries any TNT any more.
     *
     * @param context the context of the run
     */
    private void finish(TntAnimationContext context) {
        this.finished = true;
        for (Carrier carrier : this.carriers) {
            carrier.entity.setLeashHolder(null);
            context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, carrier.position, new Vec(0.2), 0.02f, 8));
            context.release(carrier.entity);
        }
    }

    /**
     * Flies the carrier one tick along its route. The first waypoint of the route is the current leg, a waypoint is
     * reached once the carrier stands on it. The carrier faces the direction it moves to.
     *
     * @param carrier the carrier to move
     * @return true if the route is complete, which means the carrier reached its last waypoint
     */
    private static boolean flyRoute(Carrier carrier) {
        Vec goal = carrier.route.peek();
        if (goal == null) return true;

        Vec current = Vec.fromPoint(carrier.position);
        double speed = legSpeed(carrier.legTicks, current.distance(goal));
        Vec next = nextPosition(current, goal, speed);
        carrier.legTicks++;
        moveTo(carrier, next);

        if (next.equals(goal)) {
            carrier.route.poll();
            carrier.legTicks = 0;
        }
        return carrier.route.isEmpty();
    }

    /**
     * Moves the carrier to the given point and turns it to the direction of the movement.
     *
     * @param carrier the carrier to move
     * @param next    the point the carrier moves to
     */
    private static void moveTo(Carrier carrier, Vec next) {
        Vec current = Vec.fromPoint(carrier.position);
        Vec delta = next.sub(current);
        float yaw = carrier.position.yaw();
        float pitch = carrier.position.pitch();
        if (delta.length() > 0) {
            yaw = (float) Math.toDegrees(Math.atan2(-delta.x(), delta.z()));
            pitch = (float) -Math.toDegrees(Math.atan2(delta.y(), Math.hypot(delta.x(), delta.z())));
        }
        carrier.position = new Pos(next, yaw, pitch);
        carrier.entity.teleport(carrier.position);
    }

    /**
     * Moves the block of TNT of the carrier one tick behind its hanging point. The block never is further away from the
     * carrier than the rope allows.
     *
     * @param carrier the carrier which carries the block
     */
    private static void swingParcel(Carrier carrier) {
        Vec carrierPosition = Vec.fromPoint(carrier.position);
        Vec swung = swing(carrier.parcelPosition, hangPoint(carrierPosition), PARCEL_FOLLOW);
        Vec next = clampRope(swung, carrierPosition, MAX_ROPE);
        carrier.parcelPosition = next;
        carrier.parcel.teleport(new Pos(next));
    }

    /**
     * Returns the speed of a carrier in one tick of a leg. The speed ramps up over the first {@link #ACCEL_TICKS} ticks
     * of the leg and slows down within {@link #BRAKE_DISTANCE} of the goal. It never drops below {@link #MIN_SPEED},
     * so every leg arrives.
     *
     * @param ticksInLeg     the number of ticks the carrier already flew in this leg
     * @param distanceToGoal the distance in blocks from the carrier to the goal of the leg
     * @return the distance in blocks the carrier flies in this tick
     */
    static double legSpeed(int ticksInLeg, double distanceToGoal) {
        double accel = Math.min(1.0, (ticksInLeg + 1.0) / ACCEL_TICKS);
        double brake = Math.min(1.0, distanceToGoal / BRAKE_DISTANCE);
        return Math.max(MIN_SPEED, FLY_SPEED * Math.min(accel, brake));
    }

    /**
     * Moves the parcel towards the hanging point by the follow share of the distance. A follow below one lets the parcel
     * lag behind a moving hanging point, so it trails and swings.
     *
     * @param parcel     the current position of the block of TNT
     * @param hangPoint  the point the block hangs at, below the carrier
     * @param follow     the share of the distance to the hanging point the parcel covers
     * @return the new position of the block of TNT
     */
    static Vec swing(Vec parcel, Vec hangPoint, double follow) {
        return parcel.add(hangPoint.sub(parcel).mul(follow));
    }

    /**
     * Pulls the parcel in on the rope when it is further away from the carrier than the maximum rope length. The
     * parcel keeps its direction from the carrier.
     *
     * @param parcel   the position of the block of TNT
     * @param carrier  the position of the carrier
     * @param maxRope  the maximum distance in blocks between the carrier and the block of TNT
     * @return the position of the block of TNT, at most the maximum rope length away from the carrier
     */
    static Vec clampRope(Vec parcel, Vec carrier, double maxRope) {
        Vec offset = parcel.sub(carrier);
        double distance = offset.length();
        if (distance <= maxRope) return parcel;
        return carrier.add(offset.mul(maxRope / distance));
    }

    /**
     * Returns the point the carrier moves to in one tick. The point moves from the current point towards the goal by
     * the speed and never overshoots, so the goal itself is returned once it is within reach.
     *
     * @param current the point the carrier is at
     * @param goal    the point to fly to
     * @param speed   the distance in blocks the carrier flies per tick
     * @return the next point, the goal once it is within the speed
     */
    static Vec nextPosition(Vec current, Vec goal, double speed) {
        Vec offset = goal.sub(current);
        double distance = offset.length();
        if (distance <= speed) return goal;
        return current.add(offset.mul(speed / distance));
    }

    /**
     * Returns the point below the carrier at which its block of TNT hangs on the rope.
     *
     * @param carrierPosition the position of the carrier
     * @return the hanging point, {@link #ROPE_LENGTH} below the carrier
     */
    private static Vec hangPoint(Vec carrierPosition) {
        return carrierPosition.sub(0, ROPE_LENGTH, 0);
    }

    /**
     * Returns the point above the target at which the carrier flies level towards the target.
     *
     * @param target the block position the TNT is delivered to
     * @return the cruise point above the centre of the target
     */
    private static Vec cruisePoint(Vec target) {
        return new Vec(target.x() + 0.5, target.y() + CRUISE_HEIGHT, target.z() + 0.5);
    }

    /**
     * Returns the point above the target at which the carrier drops its TNT. The block of TNT then hangs just above the
     * target.
     *
     * @param target the block position the TNT is delivered to
     * @return the landing point above the centre of the target
     */
    private static Vec landingPoint(Vec target) {
        return new Vec(target.x() + 0.5, target.y() + LANDING_HEIGHT, target.z() + 0.5);
    }

    /**
     * Takes the open position which is nearest to the given point, the position is removed from the open work.
     *
     * @param from the point to measure the distance from
     * @return the nearest open position, null if no position is open
     */
    private Vec takeNearestOpen(Point from) {
        if (this.openPositions.isEmpty()) return null;
        int nearest = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < this.openPositions.size(); i++) {
            Vec position = this.openPositions.get(i);
            double distance = Math.hypot(from.x() - position.x(), from.z() - position.z());
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return this.openPositions.remove(nearest);
    }

    /**
     * The phases of a carrier: it waits at the depot, flies to its target with a block of TNT on the leash, waits
     * above the target while the block swings out, and flies back to the depot.
     */
    private enum Phase {
        IDLE,
        DELIVER,
        SETTLE,
        RETURN
    }

    /**
     * The state of one carrier. The position is the last position the carrier was moved to. The route holds the
     * waypoints of the current flight, the parcel is the block of TNT on the leash, it is null while the carrier has no
     * target.
     */
    private static final class Carrier {

        private final Entity entity;
        private final Deque<Vec> route = new ArrayDeque<>();
        private Pos position;
        private Phase phase = Phase.IDLE;
        private Vec target;
        private Entity parcel;
        private Vec parcelPosition;
        private int legTicks;
        private int settleTicks;
        private int ticks;

        private Carrier(Entity entity, Vec depot) {
            this.entity = entity;
            this.position = new Pos(depot);
        }
    }
}
