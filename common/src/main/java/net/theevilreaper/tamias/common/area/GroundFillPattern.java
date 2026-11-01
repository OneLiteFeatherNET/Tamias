package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

/**
 * Defines the visual fill order a ground placement can sweep the area's positions in.
 * Adding a new pattern only requires a new constant with its own fill order - the
 * placement/batching/tracking logic itself is shared and does not need to change.
 * A pattern gets a {@link Context} for each run, so it can depend on the ground positions or on randomness.
 * <p>
 * The fill order is a key per position, a lower key is placed earlier. The key is computed once per position
 * before sorting instead of on every comparison, so even complex patterns stay cheap on large areas.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum GroundFillPattern {

    /**
     * Fills outward from the center of the area, growing like a ring.
     */
    CIRCLE(context -> GroundFillPattern.circleOrder(context.center())),

    /**
     * Fills row by row, alternating direction each row (boustrophedon/"lawn mower" pattern).
     */
    LINE(_ -> GroundFillPattern::snakeKey),

    /**
     * Grows spiral arms out of the center like a galaxy, the space between the arms fills behind them.
     * The arms are rotated randomly each run.
     */
    GALAXY(context -> GroundFillPattern.galaxyOrder(context.center(), context.random().nextDouble(Math.TAU))),

    /**
     * Grows a circle from a few random spots at once, like drops falling into a puddle, until the circles meet.
     */
    DROPS(GroundFillPattern::dropsOrder),

    /**
     * Grows irregularly from the center like moss, only from block to neighbouring block.
     * So it flows around holes and structures in the ground, separate parts of the ground grow one after another.
     */
    ORGANIC(GroundFillPattern::organicOrder);

    private static final int GALAXY_ARMS = 3;
    /**
     * How far an arm turns per block of distance to the center, in radians.
     */
    private static final double GALAXY_TWIST = 0.25;
    /**
     * How much later a block between two arms is placed compared to a block on an arm, per block of distance to the arm.
     */
    private static final double GALAXY_GAP_DELAY = 2;
    private static final int MIN_DROPS = 3;
    private static final int MAX_DROPS = 5;
    /**
     * The amount of random positions which are compared to find the next drop spot far away from the others.
     */
    private static final int DROP_SPOT_SAMPLES = 16;
    /**
     * Drop spots are only picked within this share of the largest distance to the center, so no circle starts at the edge.
     */
    private static final double DROP_INNER_SHARE = 0.6;
    /**
     * The size in blocks of the patches which slow down the organic growth, so it grows tendrils around them.
     */
    private static final int ORGANIC_PATCH_SIZE = 5;
    /**
     * The highest extra cost of a block within a slow patch, a block outside of the patches costs 1.
     */
    private static final double ORGANIC_SLOW_COST = 15;
    private static final int[][] NEIGHBOURS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    /**
     * The width of a row in the key of the line pattern, it must be larger than any x coordinate range of an area.
     */
    private static final double LINE_ROW_WIDTH = 1 << 22;

    private final Function<Context, ToDoubleFunction<Point>> orderFactory;

    GroundFillPattern(Function<Context, ToDoubleFunction<Point>> orderFactory) {
        this.orderFactory = orderFactory;
    }

    /**
     * Returns the fill order of this pattern as a key per position, a lower key is placed earlier.
     * The key only uses the x and z coordinates, so it also orders positions above the ground, e.g. TNT.
     *
     * @param context the data of the run, patterns which don't need a part of it ignore it
     * @return the key function
     */
    public ToDoubleFunction<Point> order(Context context) {
        return this.orderFactory.apply(context);
    }

    /**
     * Returns the fill order of this pattern as a comparator. It computes the key on every comparison, so it is
     * meant for small lists. Use {@link #sort(Collection, ToDoubleFunction)} for the ground positions.
     *
     * @param context the data of the run, patterns which don't need a part of it ignore it
     * @return the comparator
     */
    public Comparator<Vec> comparator(Context context) {
        return Comparator.comparingDouble(order(context)::applyAsDouble);
    }

    /**
     * Sorts the given positions by the given fill order. The key is computed once per position.
     *
     * @param positions the positions to sort
     * @param order     the fill order, a lower key is placed earlier
     * @return a new list with the sorted positions
     */
    public static List<Vec> sort(Collection<Vec> positions, ToDoubleFunction<? super Vec> order) {
        Keyed[] keyed = new Keyed[positions.size()];
        int index = 0;
        for (Vec position : positions) {
            keyed[index++] = new Keyed(position, order.applyAsDouble(position));
        }
        Arrays.sort(keyed, Comparator.comparingDouble(Keyed::key));
        List<Vec> sorted = new ArrayList<>(keyed.length);
        for (Keyed entry : keyed) {
            sorted.add(entry.position());
        }
        return sorted;
    }

    private record Keyed(Vec position, double key) { }

    /**
     * Picks a random pattern which differs from the previous one, so the same pattern never runs twice in a row.
     *
     * @param previous the pattern of the last run, null if there was none
     * @param random   the random generator to use
     * @return the picked pattern
     */
    public static GroundFillPattern next(@Nullable GroundFillPattern previous, RandomGenerator random) {
        GroundFillPattern[] patterns = values();
        if (previous == null || patterns.length == 1) return patterns[random.nextInt(patterns.length)];
        // Skips the previous pattern by drawing from the remaining ones and shifting past it
        int index = random.nextInt(patterns.length - 1);
        return patterns[index >= previous.ordinal() ? index + 1 : index];
    }

    /**
     * The data a pattern can use to build the fill order of one run.
     *
     * @param center    the center of the area
     * @param positions the ground positions which are placed
     * @param random    the random generator for patterns which look different each run
     */
    public record Context(Point center, Collection<Vec> positions, RandomGenerator random) {

        /**
         * Creates a context for patterns which only depend on the center.
         *
         * @param center the center of the area
         * @return the created context
         */
        public static Context of(Point center) {
            return new Context(center, List.of(), ThreadLocalRandom.current());
        }
    }

    /**
     * Returns a fill order which grows outward from the given center by the squared distance.
     *
     * @param center the center to grow from
     * @return the key function
     */
    static ToDoubleFunction<Point> circleOrder(Point center) {
        return pos -> {
            double dx = pos.x() - center.x();
            double dz = pos.z() - center.z();
            return dx * dx + dz * dz;
        };
    }

    /**
     * Returns the key of the line pattern: row by row along z, the x direction alternates each row.
     *
     * @param pos the position
     * @return the key
     */
    private static double snakeKey(Point pos) {
        double row = Math.floor(pos.z());
        boolean reversed = Math.floorMod((long) row, 2) != 0;
        return row * LINE_ROW_WIDTH + (reversed ? -pos.x() : pos.x());
    }

    /**
     * Returns a fill order along spiral arms around the given center.
     * A block is placed at its distance to the center plus a delay for its distance to the closest arm.
     *
     * @param center   the center of the galaxy
     * @param rotation the rotation of the arms in radians
     * @return the key function
     */
    static ToDoubleFunction<Point> galaxyOrder(Point center, double rotation) {
        double sector = Math.TAU / GALAXY_ARMS;
        return pos -> {
            double dx = pos.x() - center.x();
            double dz = pos.z() - center.z();
            double radius = Math.hypot(dx, dz);
            if (radius == 0) return 0;
            double armAngle = Math.atan2(dz, dx) - radius * GALAXY_TWIST - rotation;
            double inSector = (armAngle % sector + sector) % sector;
            double angleToArm = Math.min(inSector, sector - inSector);
            return radius + GALAXY_GAP_DELAY * angleToArm * radius;
        };
    }

    /**
     * Returns a fill order which grows circles from a random amount of random drop spots.
     *
     * @param context the data of the run
     * @return the key function
     */
    private static ToDoubleFunction<Point> dropsOrder(Context context) {
        int amount = context.random().nextInt(MIN_DROPS, MAX_DROPS + 1);
        List<Vec> inner = innerPositions(context.positions(), context.center());
        return dropsOrder(pickDropSpots(inner, amount, context.random()), context.center());
    }

    /**
     * Returns a fill order by the distance to the closest drop spot.
     *
     * @param spots  the spots the circles grow from
     * @param center the fallback spot if no spot is given
     * @return the key function
     */
    static ToDoubleFunction<Point> dropsOrder(List<Vec> spots, Point center) {
        List<Point> origins = spots.isEmpty() ? List.of(center) : List.copyOf(spots);
        return pos -> distanceToClosest(pos, origins);
    }

    /**
     * Returns the positions which are close enough to the center to be a drop spot.
     *
     * @param positions the positions of the ground
     * @param center    the center of the area
     * @return the inner positions
     */
    static List<Vec> innerPositions(Collection<Vec> positions, Point center) {
        List<Point> origin = List.of(center);
        double maxDistance = 0;
        for (Vec position : positions) {
            maxDistance = Math.max(maxDistance, distanceToClosest(position, origin));
        }
        // The distances are squared, so the share is squared as well
        double limit = maxDistance * DROP_INNER_SHARE * DROP_INNER_SHARE;
        return positions.stream().filter(position -> distanceToClosest(position, origin) <= limit).toList();
    }

    /**
     * Picks random drop spots from the given positions which are spread over the area.
     * Each further spot is the one of a few random samples which is the farthest away from the spots picked so far.
     *
     * @param positions the positions to pick from
     * @param amount    the amount of spots to pick
     * @param random    the random generator to use
     * @return the picked spots, fewer if there are not enough positions
     */
    static List<Vec> pickDropSpots(Collection<Vec> positions, int amount, RandomGenerator random) {
        List<Vec> candidates = new ArrayList<>(positions);
        List<Vec> spots = new ArrayList<>();
        if (candidates.isEmpty()) return spots;
        spots.add(candidates.remove(random.nextInt(candidates.size())));
        while (spots.size() < amount && !candidates.isEmpty()) {
            int bestIndex = -1;
            double bestDistance = -1;
            for (int i = 0; i < DROP_SPOT_SAMPLES; i++) {
                int index = random.nextInt(candidates.size());
                double distance = distanceToClosest(candidates.get(index), spots);
                if (distance > bestDistance) {
                    bestDistance = distance;
                    bestIndex = index;
                }
            }
            spots.add(candidates.remove(bestIndex));
        }
        return spots;
    }

    /**
     * Returns the squared horizontal distance from the given position to the closest of the given spots.
     *
     * @param position the position to measure from
     * @param spots    the spots to measure to
     * @return the squared distance, {@link Double#MAX_VALUE} if there are no spots
     */
    private static double distanceToClosest(Point position, List<? extends Point> spots) {
        double closest = Double.MAX_VALUE;
        for (Point spot : spots) {
            double dx = position.x() - spot.x();
            double dz = position.z() - spot.z();
            closest = Math.min(closest, dx * dx + dz * dz);
        }
        return closest;
    }

    /**
     * Returns a fill order by the time the organic growth reaches a position.
     * Positions which are not part of the ground are placed last.
     *
     * @param context the data of the run
     * @return the key function
     */
    static ToDoubleFunction<Point> organicOrder(Context context) {
        if (context.positions().isEmpty()) return circleOrder(context.center());
        Map<Long, Double> arrival = organicArrival(context.positions(), context.center(), context.random());
        return pos -> arrival.getOrDefault(columnKey(pos.blockX(), pos.blockZ()), Double.MAX_VALUE);
    }

    /**
     * Calculates when the growth reaches each position. The growth starts at the position closest to the center and
     * spreads to the neighbours, each block has a cost to grow onto it from a random field (Dijkstra with random weights).
     * Once a part of the ground is filled, the next part starts at its position closest to the center.
     *
     * @param positions the ground positions
     * @param center    the center of the area
     * @param random    the random generator for the step costs
     * @return the arrival time per column key of the positions
     */
    private static Map<Long, Double> organicArrival(Collection<Vec> positions, Point center, RandomGenerator random) {
        Set<Long> remaining = new HashSet<>();
        for (Vec position : positions) {
            remaining.add(columnKey(position.blockX(), position.blockZ()));
        }
        Map<Long, Double> arrival = new HashMap<>();
        Map<Long, Double> lattice = new HashMap<>();
        double offset = 0;
        while (!remaining.isEmpty()) {
            long start = closestTo(remaining, center);
            PriorityQueue<Step> queue = new PriorityQueue<>(Comparator.comparingDouble(Step::time));
            Map<Long, Double> best = new HashMap<>();
            queue.add(new Step(start, offset));
            best.put(start, offset);
            while (!queue.isEmpty()) {
                Step step = queue.poll();
                if (!remaining.remove(step.key())) continue;
                arrival.put(step.key(), step.time());
                offset = Math.max(offset, step.time());
                int x = (int) (step.key() >> 32);
                int z = (int) step.key();
                for (int[] neighbour : NEIGHBOURS) {
                    long next = columnKey(x + neighbour[0], z + neighbour[1]);
                    if (!remaining.contains(next)) continue;
                    double time = step.time() + organicCost(x + neighbour[0], z + neighbour[1], lattice, random);
                    if (time < best.getOrDefault(next, Double.MAX_VALUE)) {
                        best.put(next, time);
                        queue.add(new Step(next, time));
                    }
                }
            }
            offset += 1;
        }
        return arrival;
    }

    /**
     * Returns the cost to grow onto a block. The cost follows a smooth random field (value noise), so the slow blocks
     * form connected patches and the growth flows around them instead of leaving single blocks behind.
     *
     * @param x       the x coordinate of the block
     * @param z       the z coordinate of the block
     * @param lattice the random values of the patch corners, filled lazily
     * @param random  the random generator for new patch corners
     * @return the cost, at least 1
     */
    private static double organicCost(int x, int z, Map<Long, Double> lattice, RandomGenerator random) {
        int cellX = Math.floorDiv(x, ORGANIC_PATCH_SIZE);
        int cellZ = Math.floorDiv(z, ORGANIC_PATCH_SIZE);
        double fx = smooth((double) Math.floorMod(x, ORGANIC_PATCH_SIZE) / ORGANIC_PATCH_SIZE);
        double fz = smooth((double) Math.floorMod(z, ORGANIC_PATCH_SIZE) / ORGANIC_PATCH_SIZE);
        double top = lerp(corner(cellX, cellZ, lattice, random), corner(cellX + 1, cellZ, lattice, random), fx);
        double bottom = lerp(corner(cellX, cellZ + 1, lattice, random), corner(cellX + 1, cellZ + 1, lattice, random), fx);
        double noise = lerp(top, bottom, fz);
        // Only the upper part of the field slows the growth down, so most of the ground grows fast
        double slow = Math.max(0, noise - 0.5) * 2;
        return 1 + ORGANIC_SLOW_COST * slow * slow;
    }

    private static double corner(int cellX, int cellZ, Map<Long, Double> lattice, RandomGenerator random) {
        return lattice.computeIfAbsent(columnKey(cellX, cellZ), _ -> random.nextDouble());
    }

    private static double smooth(double value) {
        return value * value * (3 - 2 * value);
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private static long closestTo(Set<Long> keys, Point center) {
        long closest = 0;
        double closestDistance = Double.MAX_VALUE;
        for (long key : keys) {
            double dx = (int) (key >> 32) - center.x();
            double dz = (int) key - center.z();
            double distance = dx * dx + dz * dz;
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = key;
            }
        }
        return closest;
    }

    /**
     * Packs the x and z coordinate into one key, the height is ignored so TNT above the ground maps to its column.
     */
    private static long columnKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private record Step(long key, double time) { }
}
