package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;

/**
 * The {@link AreaDataLayer} is a record class that implements the {@link AreaData} interface.
 * It represents a layer of area data with a lower corner, upper corner, and facing a direction.
 * This class is immutable and can be constructed using the provided builder.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see AreaData
 * @since 1.0.0
 */
public record AreaDataLayer(
        Vec lowerCorner,
        Vec upperCorner,
        Direction facing
) implements AreaData {

    /**
     * Creates a new AreaDataLayer with the specified lower corner, upper corner, and facing direction.
     *
     * @param lowerCorner the lower corner of the area
     * @param upperCorner the upper corner of the area
     * @param facing      the facing direction of the area
     */
    public AreaDataLayer {
        Vec distance = upperCorner.sub(lowerCorner);
        Check.argCondition(distance.equals(Vec.ZERO), "The distance between start and end can't be zero");
    }
}
