package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.util.Helper;

import java.util.Comparator;

/**
 * Defines the visual fill order a ground placement can sweep the area's positions in.
 * Adding a new pattern only requires a new constant with its own {@link Comparator} - the
 * placement/batching/tracking logic itself is shared and does not need to change.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum GroundFillPattern {

    /**
     * Fills outward from the world origin, growing like a ring.
     */
    CIRCLE(Helper.getComparator()),

    /**
     * Fills row by row, alternating direction each row (boustrophedon/"lawn mower" pattern).
     */
    LINE(GroundFillPattern.snakeComparator());

    private final Comparator<Vec> comparator;

    GroundFillPattern(Comparator<Vec> comparator) {
        this.comparator = comparator;
    }

    /**
     * Returns the fill-order comparator for this pattern.
     *
     * @return the comparator positions should be sorted by before placing them
     */
    public Comparator<Vec> comparator() {
        return this.comparator;
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
