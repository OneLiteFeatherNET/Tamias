package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract non-sealed class AreaBasePlacement implements AreaPlacement {

    protected final Instance instance;
    protected final Set<Vec> placedPositions = ConcurrentHashMap.newKeySet();
    protected @Nullable Task buildTask;

    AreaBasePlacement(Instance instance) {
        this.instance = instance;
    }

    /**
     * Guards against starting a placement run while one is already active and resets the
     * placed-position tracking for the new run.
     * Subclasses must call this at the start of their placement run instead of manually
     * checking {@link #buildTask} so {@link #getPlacedPositions()} stays accurate for every
     * placement strategy.
     *
     * @return {@code true} if a new run may start, {@code false} if one is already running
     */
    protected final boolean tryStart() {
        if (this.buildTask != null) return false;
        this.placedPositions.clear();
        return true;
    }

    /**
     * Places a block at the given position and records it as placed.
     * Subclasses must call this instead of setting the block directly so the position shows up in
     * {@link #getPlacedPositions()}.
     *
     * @param position the position to place the block
     * @param block    the block to place
     */
    protected final void doPlaceBlock(Vec position, Block block) {
        this.instance.setBlock(position, block);
        this.placedPositions.add(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        return buildTask != null && buildTask.isAlive();
    }

    /**
     * Cancels the placement before it is done. Subclasses which keep more work running override this to cancel it too.
     */
    @Override
    public void stop() {
        endTask();
    }

    /**
     * Ends the build task when a run is done on its own, so the next run may start.
     * Unlike {@link #stop()} it doesn't cancel anything which still belongs to the run.
     */
    protected final void finish() {
        endTask();
    }

    private void endTask() {
        if (this.buildTask != null) {
            this.buildTask.cancel();
            this.buildTask = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Task getTask() {
        return this.buildTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Vec> getPlacedPositions() {
        return Set.copyOf(this.placedPositions);
    }

    /**
     * Checks if a block was placed at the given position during the current run.
     * Unlike {@link #getPlacedPositions()} this doesn't copy the placed positions, so it can be called every tick.
     *
     * @param position the position to check
     * @return true if a block was placed at the position
     */
    public boolean isPlaced(Point position) {
        return this.placedPositions.contains(position);
    }
}
