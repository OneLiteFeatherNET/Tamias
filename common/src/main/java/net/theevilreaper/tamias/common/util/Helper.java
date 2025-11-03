package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Point;

import java.util.Comparator;

public final class Helper {

    public static boolean isValidFace(double pitch) {
        return pitch >= -50 && pitch <= 50;
    }

    public static <T extends Point>  Comparator<T> getComparator() {
        return Comparator.comparing(pos -> {
            var x = pos.x();
            var z = pos.z();
            return Math.sqrt(x * x + z * z);
        });
    }

    private Helper() {
        // No instance allowed for a utility class
    }
}
