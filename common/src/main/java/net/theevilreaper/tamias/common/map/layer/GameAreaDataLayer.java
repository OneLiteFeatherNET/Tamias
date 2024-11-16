package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record GameAreaDataLayer(
        @NotNull Vec lowerCorner,
        @NotNull Vec upperCorner,
        @NotNull Direction facing
) implements GameAreaData {

    public GameAreaDataLayer {
        Vec distance = upperCorner.sub(lowerCorner);
        Check.argCondition(distance.equals(Vec.ZERO), "The distance between start and end can't be zero");
    }
}
