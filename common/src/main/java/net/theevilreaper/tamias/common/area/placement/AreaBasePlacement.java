package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract non-sealed class AreaBasePlacement implements AreaPlacement {

    protected final List<Vec> blockPositions;
    protected final Supplier<List<Vec>> tntPositions;
    protected final BlockPlaceFunction blockPlaceFunction;
    protected Task buildTask;

    AreaBasePlacement(@NotNull List<Vec> blockPositions, @NotNull Supplier<List<Vec>> tntPositions, @NotNull BlockPlaceFunction blockPlaceFunction) {
        this.blockPositions = new ArrayList<>(blockPositions);
        this.tntPositions = tntPositions;
        this.blockPlaceFunction =  blockPlaceFunction;
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
