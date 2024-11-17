package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.event.FinishBuildEvent;
import net.theevilreaper.tamias.common.util.Helper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;

public final class CircleAreaPlacement extends AreaBasePlacement{

    public CircleAreaPlacement(@NotNull Supplier<List<Vec>> blockPositions, @NotNull Supplier<List<Vec>> tntPositions, @NotNull BlockPlaceFunction blockPlaceFunction) {
        super(blockPositions, tntPositions, blockPlaceFunction);
    }

    @Override
    public void place() {
        if (this.buildTask != null) return;
        List<Vec> posList = new ArrayList<>(this.blockPositions.get());
        posList.sort(Helper.getComparator());
        LinkedBlockingDeque<Vec> queue = new LinkedBlockingDeque<>(posList);

        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<Vec> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < BLOCKS_PER_STEP; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                EventDispatcher.call(new FinishBuildEvent());
                return;
            }
            for (Vec pos : positions) {
                this.blockPlaceFunction.placeBlock(pos);
            }
            sendExpCount(queue.size());
        }).repeat(5, ChronoUnit.MILLIS).schedule();
    }
}
