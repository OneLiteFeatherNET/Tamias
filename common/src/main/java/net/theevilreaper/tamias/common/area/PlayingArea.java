package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
     * Returns the game area data.
     *
     * @return the game area data
     */
    @NotNull GameAreaData getGameAreaData();

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
