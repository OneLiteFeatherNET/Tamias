package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Contract;

public final class PosHelper {

    private static final Vec Y_VEC = new Vec(0.5, 1, 0.5);

    @Contract(pure = true, value = "_ -> new")
    public static Point getCenter2D(Point point) {
        return point.add(Y_VEC);
    }

    private PosHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
