package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link AreaDataBuilder} is a builder class for creating instances of {@link AreaData}.
 * It allows setting the lower and upper corners of the area and the facing direction.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see AreaDataLayer
 * @since 1.0.0
 */
public final class AreaDataBuilder implements AreaData.Builder {

    private @Nullable Vec lowerCorner;
    private @Nullable Vec upperCorner;
    private @Nullable Direction facing;

    /**
     * Constructs a new AreaDataBuilder instance.
     */
    AreaDataBuilder() {

    }

    /**
     * Constructs a new AreaDataBuilder instance with the specified area data.
     *
     * @param areaData the area data to use for building
     */
    AreaDataBuilder(AreaData areaData) {
        this.lowerCorner = areaData.lowerCorner();
        this.upperCorner = areaData.upperCorner();
        this.facing = areaData.facing();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaData.Builder lowerCorner(Vec lowerCorner) {
        this.lowerCorner = lowerCorner;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaData.Builder upperCorner(Vec upperCorner) {
        this.upperCorner = upperCorner;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaData.Builder facing(Direction facing) {
        this.facing = facing;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaData build() {
        return new AreaDataLayer(lowerCorner, upperCorner, facing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Direction facing() {
        return facing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Vec lowerCorner() {
        return lowerCorner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Vec upperCorner() {
        return upperCorner;
    }
}
