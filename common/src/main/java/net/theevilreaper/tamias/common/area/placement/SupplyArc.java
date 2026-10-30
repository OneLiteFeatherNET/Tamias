package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Calculates the arc on which a member of a supply drop flies from the impact to its position.
 * The same points are used for the animation and for the check if the flight is free, so a member never flies
 * through a block.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SupplyArc {

    /**
     * The ticks a member of a supply drop flies from the impact to its position.
     */
    static final int TICKS = 12;
    /**
     * The height of the apex above the straight line between the impact and the position.
     */
    static final double HEIGHT = 2.5;
    /**
     * The height of a falling block, the block above the bottom of the entity must be free as well.
     */
    private static final double ENTITY_HEIGHT = 0.9;

    private SupplyArc() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Returns the positions of the flying entity for each tick between the start and the landing.
     *
     * @param from   the block position of the impact
     * @param target the block position where the member lands
     * @return the positions of the entity, without the start and the end
     */
    public static List<Vec> points(Vec from, Vec target) {
        Vec start = from.add(0.5, 0, 0.5);
        Vec end = target.add(0.5, 0, 0.5);
        List<Vec> points = new ArrayList<>(TICKS - 1);
        for (int tick = 1; tick < TICKS; tick++) {
            double progress = (double) tick / TICKS;
            points.add(start.lerp(end, progress).add(0, 4 * HEIGHT * progress * (1 - progress), 0));
        }
        return points;
    }

    /**
     * Checks if a member can fly from the impact to its position without passing through a block.
     *
     * @param from   the block position of the impact
     * @param target the block position where the member lands
     * @param isAir  checks if the block at a block position is air
     * @return true if every block on the arc is air
     */
    public static boolean isFree(Vec from, Vec target, Predicate<Point> isAir) {
        for (Vec point : points(from, target)) {
            if (!isAir.test(point.apply(Vec.Operator.FLOOR))) return false;
            if (!isAir.test(point.add(0, ENTITY_HEIGHT, 0).apply(Vec.Operator.FLOOR))) return false;
        }
        return true;
    }
}
