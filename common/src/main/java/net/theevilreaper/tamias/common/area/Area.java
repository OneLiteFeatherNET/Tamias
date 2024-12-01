package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * With an {@link Area} it is possible to get specific positions in an area between two points.
 * What the area does with the positions is up to the implementation and can be different.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Area {

    /**
     * Calculates the positions between two points.
     */
    void calculatePositions();

    /**
     * Triggers the placement of the area.
     */
    void triggerPlacement();

    /**
     * Resets the area to the initial state.
     */
    void reset();

    /**
     * Returns the instance where the area is located.
     *
     * @return the given instance
     */
    @NotNull Instance getInstance();

    @Nullable Task getTask();
}
