package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link AreaData} represents a holder class which holds additional information about an area.
 * It holds the lower and upper corner of the area and the facing direction.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see AreaDataLayer
 * @since 1.0.0
 */
public sealed interface AreaData permits AreaDataLayer {

    /**
     * Creates a new builder reference to create a new {@link AreaData}.
     *
     * @return a new builder instance
     */
    @Contract(pure = true)
    static @NotNull Builder builder() {
        return new AreaDataBuilder();
    }

    /**
     * Creates a new builder reference to create a new {@link AreaData} with the given {@link AreaData}.
     *
     * @param areaData the area data to use for building
     * @return a new builder instance
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull Builder builder(@NotNull AreaData areaData) {
        return new AreaDataBuilder(areaData);
    }

    /**
     * The lower corner of the area as a {@link Vec}.
     *
     * @return the lower corner
     */
    @NotNull Vec lowerCorner();

    /**
     * The upper corner of the area as a {@link Vec}.
     *
     * @return the upper corner
     */
    @NotNull Vec upperCorner();

    /**
     * The facing direction of the area.
     *
     * @return the facing direction
     */
    @NotNull Direction facing();

    /**
     * The builder interface to create a new {@link AreaData}.
     *
     * @author theEvilReaper
     * @version 1.0.0
     * @see AreaDataBuilder
     * @since 1.0.0
     */
    sealed interface Builder permits AreaDataBuilder {

        /**
         * Sets the lower corner of the area.
         *
         * @param lowerCorner the lower corner of the area
         * @return the current builder instance
         */
        @NotNull Builder lowerCorner(@NotNull Vec lowerCorner);

        /**
         * Sets the upper corner of the area.
         *
         * @param upperCorner the upper corner of the area
         * @return the current builder instance
         */
        @NotNull Builder upperCorner(@NotNull Vec upperCorner);

        /**
         * Sets the facing direction of the area.
         *
         * @param facing the facing direction of the area
         * @return the current builder instance
         */
        @NotNull Builder facing(@NotNull Direction facing);

        /**
         * Builds the {@link AreaData} instance.
         *
         * @return the created {@link AreaData}
         */
        @NotNull AreaData build();

        /**
         * Returns the lower corner of the area.
         *
         * @return the lower corner
         */
        @Nullable Vec lowerCorner();

        /**
         * Returns the upper corner of the area.
         *
         * @return the upper corner
         */
        @Nullable Vec upperCorner();

        /**
         * Returns the facing direction of the area.
         *
         * @return the facing direction
         */
        @Nullable Direction facing();
    }
}
