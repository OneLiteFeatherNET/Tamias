package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.IntSupplier;

/**
 * The {@link PlayingArea} represents the area where the game is meant to be played.
 * The area is defined by the {@link Area} interface and extends it with additional information.
 * Additionally, the playing area contains the positions of special blocks and TNT.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see Area
 * @since 1.0.0
 */
public interface PlayingArea extends Area {

    /**
     * Calculates the positions amount of the special blocks.
     *
     * @param specialBlockCount the amount of special blocks
     */
    void calculateSpecialBlockPositions(@NotNull IntSupplier specialBlockCount);

    /**
     * Calculates the positions amount of the TNT blocks.
     *
     * @param tntCountCalculator the amount of TNT blocks
     */
    void calculateTntPositions(@NotNull IntSupplier tntCountCalculator);

    /**
     * Returns the game area data.
     *
     * @return the game area data
     */
    @NotNull AreaData getGameAreaData();

    /**
     * Returns the positions of the TNT blocks.
     *
     * @return the tnt positions
     */
    @NotNull Set<Point> getTntPositions();

    /**
     * Returns the positions of the special blocks.
     *
     * @return the special block positions
     */
    @NotNull Set<Point> getSpecialPositions();
}
