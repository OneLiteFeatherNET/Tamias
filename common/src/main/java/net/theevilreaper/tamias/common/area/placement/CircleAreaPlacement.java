package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.util.Helper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;

public final class CircleAreaPlacement<T extends Point> extends AreaBasePlacement<T> {

    public CircleAreaPlacement(
            @NotNull List<T> blockPositions,
            @NotNull Supplier<List<T>> tntPositions,
            @NotNull BlockPlaceFunction<T> blockPlaceFunction) {
        super(blockPositions, tntPositions, blockPlaceFunction);
    }

    @Override
    public void place() {
        if (this.buildTask != null) return;
        List<T> posList = new ArrayList<>(this.blockPositions);
        posList.sort(Helper.getComparator());
        LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<>(posList);

        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<T> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < BLOCKS_PER_STEP; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                EventDispatcher.call(new AreaFinishBuildEvent());
                return;
            }
            for (T pos : positions) {
                this.blockPlaceFunction.placeBlock(pos);
            }
            sendExpCount(queue.size());
        }).repeat(5, ChronoUnit.MILLIS).schedule();
    }
}
