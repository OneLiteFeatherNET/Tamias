package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.util.CountCalculator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
         * Sets the TNT count calculator.
         *
         * @param tntCountCalculator the tnt count calculator
         * @return the current builder instance
         */
        @NotNull Builder tntCountCalculator(@NotNull CountCalculator tntCountCalculator);

        /**
         * Sets the special count calculator.
         *
         * @param specialCountCalculator the special count calculator
         * @return the current builder instance
         */
        @NotNull Builder specialCountCalculator(@NotNull CountCalculator specialCountCalculator);

        /**
         * Builds the {@link AreaData} instance.
         *
         * @return the created {@link AreaData}
         */
        @NotNull AreaData build();
    }
}
