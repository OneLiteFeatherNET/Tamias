package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BlockPlaceFunction<T extends Point> {

    void placeBlock(@NotNull T vec);
}
