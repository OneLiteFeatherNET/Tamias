package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BlockPlaceFunction {

    void placeBlock(@NotNull Vec vec);
}
