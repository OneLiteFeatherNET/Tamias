package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Point;

import java.util.Comparator;

public final class Helper {

    public static boolean isValidFace(double pitch) {
        return pitch >= -50 && pitch <= 50;
    }

    /**
     * Returns a comparator which orders positions by their horizontal distance to the given center.
     *
     * @param center the center to measure the distance from
     * @return the comparator
     */
    public static <T extends Point> Comparator<T> getComparator(Point center) {
        return Comparator.comparingDouble(pos -> {
            var x = pos.x() - center.x();
            var z = pos.z() - center.z();
            return x * x + z * z;
        });
    }

    private Helper() {
        // No instance allowed for a utility class
    }
}
