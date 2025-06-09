package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract non-sealed class AreaBasePlacement<T extends Point> implements AreaPlacement {

    protected final List<T> blockPositions;
    protected final Supplier<List<T>> tntPositions;
    protected final BlockPlaceFunction<T> blockPlaceFunction;
    protected Task buildTask;

    AreaBasePlacement(@NotNull List<T> blockPositions, @NotNull Supplier<List<T>> tntPositions, @NotNull BlockPlaceFunction<T> blockPlaceFunction) {
        this.blockPositions = new ArrayList<>(blockPositions);
        this.tntPositions = tntPositions;
        this.blockPlaceFunction = blockPlaceFunction;
    }

    protected void sendExpCount(int currentSize) {
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            float progress = (float) currentSize / this.blockPositions.size();
            onlinePlayer.setLevel(100 - (int) (progress * 100));
            onlinePlayer.setExp(1 - progress);
        }
    }


    @Override
    public boolean isRunning() {
        return buildTask != null && buildTask.isAlive();
    }

    @Override
    public @Nullable Task getTask() {
        return this.buildTask;
    }
}
