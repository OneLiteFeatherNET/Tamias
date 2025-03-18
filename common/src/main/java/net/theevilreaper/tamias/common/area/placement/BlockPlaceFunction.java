package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link BlockPlaceFunction} is used to place a block at a specific position.
 *
 * @param <T> the type of the position
 * @author theEvilReaper
 * @version 1.0.0
 * @see AreaPlacement
 * @since 1.0.0
 */
@FunctionalInterface
public interface BlockPlaceFunction<T extends Point> {

    /**
     * Places a block at the given position
     *
     * @param position the position to place the block
     */
    void placeBlock(@NotNull T position);
}
