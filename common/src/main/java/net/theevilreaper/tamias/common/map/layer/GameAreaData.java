package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface GameAreaData permits GameAreaDataLayer {

    @Contract(pure = true)
    static @NotNull Builder builder() {
        return new GameAreaDataBuilder();
    }

    @NotNull Vec lowerCorner();

    @NotNull Vec upperCorner();

    @NotNull Direction facing();

    sealed interface Builder permits GameAreaDataBuilder {

        @NotNull Builder lowerCorner(@NotNull Vec lowerCorner);

        @NotNull Builder upperCorner(@NotNull Vec upperCorner);

        @NotNull Builder facing(@NotNull Direction facing);

        @NotNull GameAreaData build();
    }
}
