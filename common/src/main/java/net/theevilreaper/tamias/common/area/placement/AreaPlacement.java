package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.timer.Task;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link AreaPlacement} is used to place blocks in a specific area.
 * The implementation can be different and depends on the implementation.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see AreaBasePlacement
 * @since 1.0.0
 */
public sealed interface AreaPlacement permits AreaBasePlacement {

    /**
     * Checks if the placement is running.
     *
     * @return {@code true} if the placement is running
     */
    boolean isRunning();

    /**
     * Starts the placement of the area.
     */
    void place();

    /**
     * Returns the current task.
     * The return type can be nullable, if the task is not running.
     *
     * @return the current task
     */
    @Nullable Task getTask();
}
