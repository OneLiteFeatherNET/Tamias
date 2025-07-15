package net.theevilreaper.tamias.common.area.holder;

import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a placement which can be triggered.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Placement {

    /**
     * Clears the placement.
     */
    void clear();

    /**
     * Triggers the placement.
     */
    void triggerPlacement(@NotNull GroundData groundData);
}
