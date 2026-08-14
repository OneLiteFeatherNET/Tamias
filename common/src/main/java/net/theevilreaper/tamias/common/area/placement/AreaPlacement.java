package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
     * Stars the placement of the area.
     */
    void place(GroundData groundData);

    /**
     * Checks if the placement is running.
     *
     * @return {@code true} if the placement is running
     */
    boolean isRunning();

    /**
     * Stops the active placement task if running.
     */
    void stop();

    /**
     * Replaces the positions this placement operates on.
     * Used to refresh the placement once the area has been scanned against the live instance,
     * since the positions supplied at construction time may not yet reflect the actual terrain.
     *
     * @param positions the new positions to use
     */
    void updatePositions(List<Vec> positions);

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
