package net.theevilreaper.tamias.common.area.placement;

import net.theevilreaper.tamias.common.ground.GroundData;

/**
 * The {@link AreaPlacement} is used to place blocks in a specific area.
 * The implementation can be different and depends on the implementation.
 *
 * @author theEvilReaper
 * @version 1.1.0
 * @see AreaBasePlacement
 * @since 0.1.0
 */
public sealed interface AreaPlacement permits AreaBasePlacement {

    /**
     * Stars the placement of the area.
     */
    void place(GroundData groundData);

    /**
     * Checks if the placement is running.
     *
     * @return {@code true} if the placement is running
     */
    boolean isRunning();
}
