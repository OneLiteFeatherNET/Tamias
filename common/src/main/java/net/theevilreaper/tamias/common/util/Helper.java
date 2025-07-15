package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@ApiStatus.NonExtendable
public final class Helper {

    public static boolean isValidFace(double pitch) {
        return pitch >= -50 && pitch <= 50;
    }

    public static <T extends Point>  @NotNull Comparator<T> getComparator() {
        return Comparator.comparing(pos -> {
            var x = pos.x();
            var z = pos.z();
            return Math.sqrt(x * x + z * z);
        });
    }

    private Helper() { }
}
