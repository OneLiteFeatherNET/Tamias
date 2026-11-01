package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.util.Helper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * Defines the visual fill order a ground placement can sweep the area's positions in.
 * Adding a new pattern only requires a new constant with its own {@link Comparator} - the
 * placement/batching/tracking logic itself is shared and does not need to change.
 * A pattern gets a {@link Context} for each run, so it can depend on the ground positions or on randomness.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum GroundFillPattern {

    /**
     * Fills outward from the center of the area, growing like a ring.
     */
    CIRCLE(context -> Helper.getComparator(context.center())),

    /**
     * Fills row by row, alternating direction each row (boustrophedon/"lawn mower" pattern).
     */
    LINE(_ -> GroundFillPattern.snakeComparator()),

    /**
     * Grows spiral arms out of the center like a galaxy, the space between the arms fills behind them.
     * The arms are rotated randomly each run.
     */
    GALAXY(context -> GroundFillPattern.galaxyComparator(context.center(), context.random().nextDouble(Math.TAU))),

    /**
     * Grows a circle from a few random spots at once, like drops falling into a puddle, until the circles meet.
     */
    DROPS(GroundFillPattern::dropsComparator);

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

    private final Function<Context, Comparator<Vec>> comparatorFactory;

    GroundFillPattern(Function<Context, Comparator<Vec>> comparatorFactory) {
        this.comparatorFactory = comparatorFactory;
    }

    /**
     * Returns the fill-order comparator for this pattern.
     * The comparator only uses the x and z coordinates, so it also orders positions above the ground, e.g. TNT.
     *
     * @param context the data of the run, patterns which don't need a part of it ignore it
     * @return the comparator positions should be sorted by before placing them
     */
    public Comparator<Vec> comparator(Context context) {
        return this.comparatorFactory.apply(context);
    }

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
     * Returns a comparator which orders positions along spiral arms around the given center.
     * A block is placed at its distance to the center plus a delay for its distance to the closest arm.
     *
     * @param center   the center of the galaxy
     * @param rotation the rotation of the arms in radians
     * @return the comparator
     */
    static Comparator<Vec> galaxyComparator(Point center, double rotation) {
        double sector = Math.TAU / GALAXY_ARMS;
        return Comparator.comparingDouble(pos -> {
            double dx = pos.x() - center.x();
            double dz = pos.z() - center.z();
            double radius = Math.hypot(dx, dz);
            if (radius == 0) return 0;
            double armAngle = Math.atan2(dz, dx) - radius * GALAXY_TWIST - rotation;
            double inSector = (armAngle % sector + sector) % sector;
            double angleToArm = Math.min(inSector, sector - inSector);
            return radius + GALAXY_GAP_DELAY * angleToArm * radius;
        });
    }

    /**
     * Returns a comparator which grows circles from a random amount of random drop spots.
     *
     * @param context the data of the run
     * @return the comparator
     */
    private static Comparator<Vec> dropsComparator(Context context) {
        int amount = context.random().nextInt(MIN_DROPS, MAX_DROPS + 1);
        List<Vec> inner = innerPositions(context.positions(), context.center());
        return dropsComparator(pickDropSpots(inner, amount, context.random()), context.center());
    }

    /**
     * Returns a comparator which orders positions by their distance to the closest drop spot.
     *
     * @param spots  the spots the circles grow from
     * @param center the fallback spot if no spot is given
     * @return the comparator
     */
    static Comparator<Vec> dropsComparator(List<Vec> spots, Point center) {
        List<Point> origins = spots.isEmpty() ? List.of(center) : List.copyOf(spots);
        return Comparator.comparingDouble(pos -> distanceToClosest(pos, origins));
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

    private static Comparator<Vec> snakeComparator() {
        return (a, b) -> {
            int rowCompare = Double.compare(a.z(), b.z());
            if (rowCompare != 0) return rowCompare;

            boolean reversed = Math.floorMod((long) Math.floor(a.z()), 2) != 0;
            int columnCompare = Double.compare(a.x(), b.x());
            return reversed ? -columnCompare : columnCompare;
        };
    }
}
