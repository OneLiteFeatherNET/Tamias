package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public final class GameAreaDataBuilder implements GameAreaData.Builder {

    private Vec lowerCorner;
    private Vec upperCorner;
    private Direction facing;


    GameAreaDataBuilder() {

    }

    @Override
    public GameAreaData.@NotNull Builder lowerCorner(@NotNull Vec lowerCorner) {
        this.lowerCorner = lowerCorner;
        return this;
    }

    @Override
    public GameAreaData.@NotNull Builder upperCorner(@NotNull Vec upperCorner) {
        this.upperCorner = upperCorner;
        return this;
    }

    @Override
    public GameAreaData.@NotNull Builder facing(@NotNull Direction facing) {
        this.facing = facing;
        return this;
    }

    @Override
    public @NotNull GameAreaData build() {
        return new GameAreaDataLayer(lowerCorner, upperCorner, facing);
    }
}
