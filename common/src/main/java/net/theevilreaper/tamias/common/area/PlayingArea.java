package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.map.layer.AreaData;

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
     * Calculates the position number of the special blocks.
     *
     * @param specialBlockCount the number of special blocks
     */
    void calculateSpecialBlockPositions(IntSupplier specialBlockCount);

    /**
     * Calculates the positions number of the TNT blocks.
     *
     * @param tntCountCalculator the number of TNT blocks
     */
    void calculateTntPositions(IntSupplier tntCountCalculator);

    /**
     * Returns the game area data.
     *
     * @return the game area data
     */
    AreaData getGameAreaData();

    /**
     * Returns the positions of the TNT blocks.
     *
     * @return the tnt positions
     */
    Set<Vec> getTntPositions();

    /**
     * Returns the positions of the special blocks.
     *
     * @return the special block positions
     */
    Set<Vec> getSpecialPositions();
}
