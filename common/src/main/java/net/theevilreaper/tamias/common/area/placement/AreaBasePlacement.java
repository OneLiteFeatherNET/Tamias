package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract non-sealed class AreaBasePlacement implements AreaPlacement {

    protected final Instance instance;
    protected final List<Vec> blockPositions;
    protected final Set<Vec> placedPositions = ConcurrentHashMap.newKeySet();
    protected @Nullable Task buildTask;

    AreaBasePlacement(Instance instance, List<Vec> blockPositions) {
        this.instance = instance;
        this.blockPositions = new ArrayList<>(blockPositions);
    }

    /**
     * Guards against starting a placement run while one is already active and resets the
     * placed-position tracking for the new run.
     * Subclasses must call this at the start of {@link #place(GroundData)} instead of manually
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
     * Subclasses must call this instead of {@link #placeBlock(Vec, GroundData)} directly so the
     * position shows up in {@link #getPlacedPositions()}.
     *
     * @param position   the position to place the block
     * @param groundData the ground data to place with
     */
    protected final void doPlaceBlock(Vec position, GroundData groundData) {
        placeBlock(position, groundData);
        this.placedPositions.add(position);
    }

    protected void sendExpCount(int currentSize) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            float progress = (float) currentSize / this.blockPositions.size();
            onlinePlayer.setLevel(100 - (int) (progress * 100));
            onlinePlayer.setExp(1 - progress);
        }
    }

    /**
     * Places a block at the given position
     *
     * @param position the position to place the block
     */
    protected abstract void placeBlock(Vec position, GroundData groundData);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        return buildTask != null && buildTask.isAlive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
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
    public void updatePositions(List<Vec> positions) {
        this.blockPositions.clear();
        this.blockPositions.addAll(positions);
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
