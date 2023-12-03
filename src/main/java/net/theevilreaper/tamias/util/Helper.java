package net.theevilreaper.tamias.util;

import net.minestom.server.coordinate.Vec;

import java.util.Comparator;

public class Helper {

    public static boolean isValidFace(double pitch) {
        return pitch >= -50 && pitch <= 50;
    }

    public static Comparator<Vec> getComparator() {
        return Comparator.comparing(pos -> {
            var x = pos.x();
            var z = pos.z();
            return Math.sqrt(x * x + z * z);
        });
    }
}
