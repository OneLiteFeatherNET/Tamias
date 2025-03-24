package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public final class AreaDataBuilder implements AreaData.Builder {

    private Vec lowerCorner;
    private Vec upperCorner;
    private Direction facing;

    AreaDataBuilder() {

    }

    @Override
    public AreaData.@NotNull Builder lowerCorner(@NotNull Vec lowerCorner) {
        this.lowerCorner = lowerCorner;
        return this;
    }

    @Override
    public AreaData.@NotNull Builder upperCorner(@NotNull Vec upperCorner) {
        this.upperCorner = upperCorner;
        return this;
    }

    @Override
    public AreaData.@NotNull Builder facing(@NotNull Direction facing) {
        this.facing = facing;
        return this;
    }

    @Override
    public @NotNull AreaData build() {
        return new AreaDataLayer(lowerCorner, upperCorner, facing);
    }
}
