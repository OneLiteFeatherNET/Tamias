package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.util.Helper;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;

public final class CircleAreaPlacement extends AreaBasePlacement<Vec> {

    private final List<Vec> specialPositions;

    public CircleAreaPlacement(
            @NotNull Instance instance,
            @NotNull List<Vec> blockPositions,
            @NotNull List<Vec> specialPositions) {
        super(instance, blockPositions);
        this.specialPositions = specialPositions;
    }

    @Override
    public void place(@NotNull GroundData groundData) {
        if (this.buildTask != null) return;
        List<Vec> posList = new ArrayList<>(this.blockPositions);
        posList.sort(Helper.getComparator());
        LinkedBlockingDeque<Vec> queue = new LinkedBlockingDeque<>(posList);

        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<Vec> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < BLOCKS_PER_STEP; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                EventDispatcher.call(new AreaFinishBuildEvent());
                return;
            }
            for (Vec pos : positions) {
                this.placeBlock(pos, groundData);
            }
            sendExpCount(queue.size());
        }).repeat(5, ChronoUnit.MILLIS).schedule();
    }

    @Override
    protected void placeBlock(@NotNull Vec position, @NotNull GroundData groundData) {
        Block groundBlock;
        if (this.specialPositions.contains(position)) {
            groundBlock = groundData.getAddtionalBlock();
        } else {
            groundBlock = groundData.groundBlock();
        }
        this.instance.setBlock(position, groundBlock);
    }
}
