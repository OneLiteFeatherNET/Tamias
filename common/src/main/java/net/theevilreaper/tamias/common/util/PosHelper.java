package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class PosHelper {

    private static final Vec Y_VEC = new Vec(0.5, 1, 0.5);

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull Point getCenter2D(@NotNull Point point) {
        return point.add(Y_VEC);
    }

    private PosHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
