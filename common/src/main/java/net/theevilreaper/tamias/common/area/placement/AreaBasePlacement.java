package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract non-sealed class AreaBasePlacement<T extends Point> implements AreaPlacement {

    protected final Instance instance;
    protected final List<T> blockPositions;
    protected Task buildTask;

    AreaBasePlacement(@NotNull Instance instance, @NotNull List<T> blockPositions) {
        this.instance = instance;
        this.blockPositions = new ArrayList<>(blockPositions);
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
    protected abstract void placeBlock(@NotNull T position, @NotNull GroundData groundData);

    @Override
    public boolean isRunning() {
        return buildTask != null && buildTask.isAlive();
    }

    @Override
    public @Nullable Task getTask() {
        return this.buildTask;
    }
}
