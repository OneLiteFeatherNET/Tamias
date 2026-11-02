package net.theevilreaper.tamias.common.area.placement.animation;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.area.placement.TntGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * A {@link TntAnimation} in which villagers rush around and place the TNT. Every worker takes the nearest position
 * whose ground is ready, walks to it with a hectic jitter and places the TNT once it has reached the block.
 * Workers without open work wander around their spawn point. The workers are driven by the scheduler of the
 * placement, not by pathfinding, so a worker can never get stuck on ground which is still being built.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VillagerTntAnimation implements TntAnimation {

    /**
     * The amount of villagers which place the TNT, fewer are spawned if the run has fewer positions.
     */
    static final int WORKER_COUNT = 5;
    /**
     * The distance in blocks a worker walks per tick towards its target.
     */
    static final double WORKER_SPEED = 0.55;
    /**
     * The horizontal distance in blocks at which a worker has reached its target and places the TNT.
     */
    static final double WORKER_REACH = 0.8;
    /**
     * The distance from the centre of the target at which a worker stops to place the TNT. It is outside the target
     * cell, so the TNT never appears on the cell the worker stands in.
     */
    static final double APPROACH_DISTANCE = 0.75;
    /**
     * The maximum sideways offset in blocks a worker takes per tick while walking.
     */
    static final double JITTER = 0.15;
    /**
     * The height in blocks of a worker's hop while walking.
     */
    static final double HOP_HEIGHT = 0.3;
    /**
     * How fast a worker hops, the hop is the absolute sine of the tick counter times this factor.
     */
    static final double HOP_FREQUENCY = 0.5;
    /**
     * The radius in blocks around the spawn point in which a worker without open work wanders.
     */
    static final double WANDER_RADIUS = 2;
    /**
     * The distance in blocks a worker wanders per tick.
     */
    static final double WANDER_SPEED = WORKER_SPEED / 2;
    /**
     * A villager sound is played every this many landings.
     */
    static final int SOUND_INTERVAL = 3;
    /**
     * The safety limit in ticks a worker with a target may go without getting closer to it, e.g. because it stands
     * still or two workers keep pushing each other away. Such a worker places its TNT from where it stands, so a run
     * can never hang.
     */
    static final int MAX_STUCK_TICKS = 40;
    /**
     * The distance in blocks the workers try to keep to each other, so two villagers hardly overlap.
     */
    static final double SEPARATION_DISTANCE = 1.0;
    /**
     * The largest step in blocks per tick a worker makes to get away from a worker which is too close.
     */
    static final double SEPARATION_PUSH = 0.4;
    private static final double PROGRESS_EPSILON = 0.05;
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));

    private final RandomGenerator random;
    private final List<Vec> openPositions = new ArrayList<>();
    private final List<Worker> workers = new ArrayList<>();
    private int totalPositions;
    private int landedCount;
    private boolean finished;

    /**
     * Creates the animation with the {@link ThreadLocalRandom} as random source.
     */
    public VillagerTntAnimation() {
        this(ThreadLocalRandom.current());
    }

    /**
     * Creates the animation with the given random source, the jitter and the wandering use it.
     *
     * @param random the random source of the run
     */
    public VillagerTntAnimation(RandomGenerator random) {
        this.random = random;
    }

    /**
     * Spawns the workers at the average position of all groups. The workers are spawned before any position is
     * ready, so they wander until the ground of a group is placed.
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
        double y = groups.getFirst().center().y();
        Predicate<Vec> passable = cell -> GridRoute.isPassable(context.instance(), cell);
        Vec average = GridRoute.cell(sumX / allPositions.size(), y, sumZ / allPositions.size());
        Vec base = GridRoute.nearestPassable(average, passable, GridRoute.MAX_SPAWN_RADIUS);

        // Every worker gets its own cell, so they don't stand inside each other at the start
        Set<Vec> used = new HashSet<>();
        int count = Math.min(WORKER_COUNT, allPositions.size());
        for (int i = 0; i < count; i++) {
            Vec cell = GridRoute.nearestPassable(base, candidate -> passable.test(candidate) && !used.contains(candidate),
                    GridRoute.MAX_SPAWN_RADIUS);
            used.add(cell);
            Vec home = centre(cell);
            Entity entity = new Entity(EntityType.VILLAGER);
            entity.setNoGravity(true);
            entity.setInstance(context.instance(), home);
            context.track(entity);

            Worker worker = new Worker(entity, home);
            this.workers.add(worker);
            context.schedule(1, () -> step(worker, context));
        }
    }

    /**
     * Adds the positions of the group to the open work, a worker only goes to a position once it is added here.
     *
     * @param group   the group whose ground is ready
     * @param context the context of the run
     */
    @Override
    public void play(TntGroup group, TntAnimationContext context) {
        this.openPositions.addAll(group.positions());
    }

    /**
     * Moves one worker by one tick. A worker without a target takes the nearest open position, if there is none it
     * wanders. A worker which reached its target places the TNT.
     *
     * @param worker  the worker to move
     * @param context the context of the run
     */
    private void step(Worker worker, TntAnimationContext context) {
        if (this.finished) return;
        worker.ticks++;
        Predicate<Vec> passable = cell -> GridRoute.isPassable(context.instance(), cell);
        if (worker.target == null) {
            worker.target = takeNearestOpen(worker.entity.getPosition());
            if (worker.target != null) {
                worker.route = planRoute(worker, passable);
                worker.closestDistance = Double.MAX_VALUE;
            }
        }

        if (worker.target == null) {
            wander(worker, passable);
        } else if (horizontalDistance(worker.entity.getPosition(), centre(worker.target)) <= WORKER_REACH
                && !GridRoute.cellOf(worker.entity.getPosition()).equals(worker.target)) {
            land(worker, context);
        } else {
            walk(worker, context, passable);
        }

        if (!this.finished) {
            context.schedule(1, () -> step(worker, context));
        }
    }

    /**
     * Places the TNT of the worker's target, plays the effects and finishes the run with the last landing.
     *
     * @param worker  the worker which reached its target
     * @param context the context of the run
     */
    private void land(Worker worker, TntAnimationContext context) {
        Vec position = worker.target;
        worker.target = null;
        worker.route = null;
        worker.stuckTicks = 0;
        worker.closestDistance = Double.MAX_VALUE;
        context.land(position);
        this.landedCount++;

        Instance instance = context.instance();
        Vec center = centre(position);
        instance.sendGroupedPacket(new EntityAnimationPacket(worker.entity.getEntityId(),
                EntityAnimationPacket.Animation.SWING_MAIN_ARM));
        instance.sendGroupedPacket(new ParticlePacket(Particle.POOF, center, new Vec(0.2), 0.02f, 8));
        instance.playSound(FallingTntAnimation.landingSound(), center.x(), center.y(), center.z());
        if (this.landedCount % SOUND_INTERVAL == 0) {
            Sound villagerSound = Sound.sound(SoundEvent.ENTITY_VILLAGER_AMBIENT, Sound.Source.NEUTRAL,
                    1f, random.nextFloat(0.9f, 1.1f));
            instance.playSound(villagerSound, center.x(), center.y(), center.z());
        }

        if (this.landedCount == this.totalPositions) {
            finish(context);
        }
    }

    /**
     * Ends the run: every worker makes a last poof and is removed. All positions have landed, so no worker has a target.
     *
     * @param context the context of the run
     */
    private void finish(TntAnimationContext context) {
        this.finished = true;
        for (Worker worker : this.workers) {
            Point position = worker.entity.getPosition().add(0, 1, 0);
            context.instance().sendGroupedPacket(new ParticlePacket(Particle.POOF, position, new Vec(0.2), 0.02f, 8));
            context.release(worker.entity);
        }
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
            double distance = horizontalDistance(from, centre(this.openPositions.get(i)));
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return this.openPositions.remove(nearest);
    }

    /**
     * Plans the route of the worker to its target over passable cells, null if there is no route.
     *
     * @param worker   the worker with its target
     * @param passable checks if a cell can be walked on
     * @return the cells from the next step to the target, null if there is no route
     */
    private static List<Vec> planRoute(Worker worker, Predicate<Vec> passable) {
        return GridRoute.find(GridRoute.cellOf(worker.entity.getPosition()), worker.target, passable,
                GridRoute.MAX_ROUTE_NODES);
    }

    /**
     * Walks the worker one tick along its route. A next cell which became blocked, e.g. by fresh TNT, is replanned
     * once. Without a route the worker places the TNT from where it stands, so it never waits forever.
     *
     * @param worker   the worker which walks
     * @param context  the context of the run
     * @param passable checks if a cell can be walked on
     */
    private void walk(Worker worker, TntAnimationContext context, Predicate<Vec> passable) {
        if (worker.route == null || worker.route.isEmpty()) {
            land(worker, context);
            return;
        }
        if (!passable.test(worker.route.getFirst())) {
            worker.route = planRoute(worker, passable);
            worker.closestDistance = Double.MAX_VALUE;
            if (worker.route == null || worker.route.isEmpty() || !passable.test(worker.route.getFirst())) {
                land(worker, context);
                return;
            }
        }
        // The last step stops at the approach point, so the worker never stands inside the target cell it places on
        boolean last = worker.route.size() == 1;
        Point goal = last ? approachPoint(worker.entity.getPosition(), centre(worker.route.getFirst()))
                : centre(worker.route.getFirst());
        if (moveTowards(worker, goal, WORKER_SPEED, passable) && !last) {
            worker.route.removeFirst();
        }
        trackProgress(worker, context);
    }

    /**
     * Counts the ticks in which the worker didn't get closer to its target along its route than ever before. After
     * {@link #MAX_STUCK_TICKS} of them it places its TNT from where it stands. The way left is measured along the
     * route, so a detour around a wall still counts as progress.
     *
     * @param worker  the worker which walked
     * @param context the context of the run
     */
    private void trackProgress(Worker worker, TntAnimationContext context) {
        if (worker.target == null || worker.route == null || worker.route.isEmpty()) return;
        double distance = worker.route.size() - 1
                + horizontalDistance(worker.entity.getPosition(), centre(worker.route.getFirst()));
        if (distance < worker.closestDistance - PROGRESS_EPSILON) {
            worker.closestDistance = distance;
            worker.stuckTicks = 0;
            return;
        }
        worker.stuckTicks++;
        if (worker.stuckTicks >= MAX_STUCK_TICKS) {
            land(worker, context);
        }
    }

    /**
     * Returns the point on the line from the target centre towards the worker, at {@link #APPROACH_DISTANCE} from the
     * centre. That point lies outside the target cell, so the worker can place the TNT from there.
     */
    private static Point approachPoint(Point worker, Vec targetCentre) {
        double dx = worker.x() - targetCentre.x();
        double dz = worker.z() - targetCentre.z();
        double distance = Math.hypot(dx, dz);
        if (distance < 1e-6) return targetCentre;
        return new Vec(
                targetCentre.x() + dx / distance * APPROACH_DISTANCE,
                targetCentre.y(),
                targetCentre.z() + dz / distance * APPROACH_DISTANCE
        );
    }

    /**
     * Lets the worker wander around its spawn point in a random direction. A wander goal which is not passable is
     * skipped, the worker stays put for this tick.
     *
     * @param worker   the worker which wanders
     * @param passable checks if a cell can be walked on
     */
    private void wander(Worker worker, Predicate<Vec> passable) {
        if (worker.wanderGoal == null || horizontalDistance(worker.entity.getPosition(), worker.wanderGoal) < 0.5) {
            double angle = this.random.nextDouble(Math.TAU);
            double radius = this.random.nextDouble(WANDER_RADIUS);
            worker.wanderGoal = new Vec(
                    worker.home.x() + Math.cos(angle) * radius,
                    worker.home.y(),
                    worker.home.z() + Math.sin(angle) * radius
            );
            if (!passable.test(GridRoute.cellOf(worker.wanderGoal))) {
                worker.wanderGoal = null;
                return;
            }
        }
        if (worker.wanderGoal != null) {
            moveTowards(worker, worker.wanderGoal, WANDER_SPEED, passable);
        }
    }

    /**
     * Moves the worker towards the goal by the given speed, with a sideways jitter and a hop. The jitter is only used
     * if it keeps the worker on a passable cell, otherwise the worker moves straight. A straight step into a
     * non-passable cell keeps the worker where it is. The worker faces the direction it moves to.
     *
     * @param worker   the worker to move
     * @param goal     the point to move towards
     * @param speed    the distance in blocks to move in this tick
     * @param passable checks if a cell can be walked on
     * @return true if the goal was reached in this tick
     */
    private boolean moveTowards(Worker worker, Point goal, double speed, Predicate<Vec> passable) {
        Pos current = worker.entity.getPosition();
        double dx = goal.x() - current.x();
        double dz = goal.z() - current.z();
        double distance = Math.hypot(dx, dz);
        if (distance < 1e-6) return true;

        double directionX = dx / distance;
        double directionZ = dz / distance;
        double x;
        double z;
        boolean reached = distance <= speed;
        if (reached) {
            x = goal.x();
            z = goal.z();
        } else {
            Vec here = GridRoute.cell(current.x(), worker.home.y(), current.z());
            double straightX = current.x() + directionX * speed;
            double straightZ = current.z() + directionZ * speed;
            if (!isWalkable(straightX, straightZ, here, passable)) {
                straightX = current.x();
                straightZ = current.z();
            }
            double sideways = this.random.nextDouble(-JITTER, JITTER);
            double jitteredX = straightX - directionZ * sideways;
            double jitteredZ = straightZ + directionX * sideways;
            if (straightX != current.x() || straightZ != current.z()) {
                if (isWalkable(jitteredX, jitteredZ, here, passable)) {
                    straightX = jitteredX;
                    straightZ = jitteredZ;
                }
            }
            x = straightX;
            z = straightZ;
        }
        Vec pushed = separate(worker, x, z, passable);
        x = pushed.x();
        z = pushed.z();
        double y = worker.home.y() + HOP_HEIGHT * Math.abs(Math.sin(worker.ticks * HOP_FREQUENCY));
        float yaw = (float) Math.toDegrees(Math.atan2(-directionX, directionZ));
        worker.entity.teleport(new Pos(x, y, z, yaw, 0f));
        return reached;
    }

    /**
     * Checks if a position may be entered: it is walkable when it stays on the cell it is on or the cell is passable.
     */
    /**
     * Moves the given point away from every other worker which is closer than {@link #SEPARATION_DISTANCE}, the
     * closer the other worker, the stronger the push. A push into a cell which can't be walked on is skipped.
     *
     * @param worker   the worker which moves
     * @param x        the x the worker wants to move to
     * @param z        the z the worker wants to move to
     * @param passable checks if a cell can be walked on
     * @return the point the worker moves to
     */
    private Vec separate(Worker worker, double x, double z, Predicate<Vec> passable) {
        double pushX = 0;
        double pushZ = 0;
        for (int i = 0; i < this.workers.size(); i++) {
            Worker other = this.workers.get(i);
            if (other == worker) continue;
            Pos position = other.entity.getPosition();
            double dx = x - position.x();
            double dz = z - position.z();
            double distance = Math.hypot(dx, dz);
            if (distance >= SEPARATION_DISTANCE) continue;
            if (distance < 1e-6) {
                // Two workers on the same spot are pushed apart in a direction which differs per worker
                double angle = this.workers.indexOf(worker) * GOLDEN_ANGLE;
                dx = Math.cos(angle);
                dz = Math.sin(angle);
                distance = 1;
            }
            double strength = SEPARATION_PUSH * (1 - Math.hypot(x - position.x(), z - position.z()) / SEPARATION_DISTANCE);
            pushX += dx / distance * strength;
            pushZ += dz / distance * strength;
        }
        if (pushX == 0 && pushZ == 0) return new Vec(x, 0, z);
        Pos current = worker.entity.getPosition();
        Vec here = GridRoute.cell(current.x(), worker.home.y(), current.z());
        double pushedX = x + pushX;
        double pushedZ = z + pushZ;
        if (!isWalkable(pushedX, pushedZ, here, passable)) return new Vec(x, 0, z);
        return new Vec(pushedX, 0, pushedZ);
    }

    private boolean isWalkable(double x, double z, Vec here, Predicate<Vec> passable) {
        Vec cell = GridRoute.cell(x, here.y(), z);
        return cell.equals(here) || passable.test(cell);
    }

    private static Vec centre(Point position) {
        return new Vec(position.x() + 0.5, position.y(), position.z() + 0.5);
    }

    private static double horizontalDistance(Point a, Point b) {
        return Math.hypot(a.x() - b.x(), a.z() - b.z());
    }

    /**
     * The state of one worker. The home is the spawn point, the wander goal is the point the worker wanders to.
     */
    private static final class Worker {

        private final Entity entity;
        private final Vec home;
        private Vec target;
        private List<Vec> route;
        private Vec wanderGoal;
        private int ticks;
        private int stuckTicks;
        private double closestDistance = Double.MAX_VALUE;

        private Worker(Entity entity, Vec home) {
            this.entity = entity;
            this.home = home;
        }
    }
}
