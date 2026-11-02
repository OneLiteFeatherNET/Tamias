package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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
     * Checks if the placement is running.
     *
     * @return {@code true} if the placement is running
     */
    boolean isRunning();

    /**
     * Cancels the active placement run if one is running. A cancelled run is not completed.
     */
    void stop();

    /**
     * Returns the positions that have already been placed by the current (or last finished) run.
     * Tracked automatically by every implementation - consumers that depend on "only where a block
     * has actually been placed so far" (e.g. dropping TNT only onto finished ground) should read
     * this instead of assuming anything about a specific placement strategy's internal order.
     *
     * @return an unmodifiable view of the placed positions
     */
    Set<Vec> getPlacedPositions();

    /**
     * Returns the current task.
     * The return type can be nullable if the task is not running.
     *
     * @return the current task
     */
    @Nullable Task getTask();
}
