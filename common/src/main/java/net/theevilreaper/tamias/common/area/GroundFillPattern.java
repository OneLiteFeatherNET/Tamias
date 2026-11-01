package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.util.Helper;
import org.jetbrains.annotations.Nullable;

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
    GALAXY(context -> GroundFillPattern.galaxyComparator(context.center(), context.random().nextDouble(Math.TAU)));

    private static final int GALAXY_ARMS = 3;
    /**
     * How far an arm turns per block of distance to the center, in radians.
     */
    private static final double GALAXY_TWIST = 0.25;
    /**
     * How much later a block between two arms is placed compared to a block on an arm, per block of distance to the arm.
     */
    private static final double GALAXY_GAP_DELAY = 2;

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
