package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.ground.GroundData;

import net.minestom.server.timer.TaskSchedule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;

/**
 * Places ground blocks in the order defined by the given {@link Comparator}, batched over ticks.
 * The fill order is fully pluggable - see {@link net.theevilreaper.tamias.common.area.GroundFillPattern}
 * for the available patterns - the batching/tracking logic here stays the same for all of them.
 */
public final class SweepAreaPlacement extends AreaBasePlacement<Vec> {

    private final List<Vec> specialPositions;
    private final Comparator<Vec> fillOrder;

    public SweepAreaPlacement(
            Instance instance,
            List<Vec> blockPositions,
            List<Vec> specialPositions,
            Comparator<Vec> fillOrder) {
        super(instance, blockPositions);
        this.specialPositions = specialPositions;
        this.fillOrder = fillOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void place(GroundData groundData) {
        if (!tryStart()) return;
        List<Vec> posList = new ArrayList<>(this.blockPositions);
        posList.sort(this.fillOrder);
        LinkedBlockingDeque<Vec> queue = new LinkedBlockingDeque<>(posList);

        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<Vec> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < BLOCKS_PER_STEP; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                stop();
                EventDispatcher.call(new AreaFinishBuildEvent());
                return;
            }
            for (Vec pos : positions) {
                this.doPlaceBlock(pos, groundData);
            }
            sendExpCount(queue.size());
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void placeBlock(Vec position, GroundData groundData) {
        Block groundBlock;
        if (this.specialPositions.contains(position)) {
            groundBlock = groundData.getAddtionalBlock();
        } else {
            groundBlock = groundData.groundBlock();
        }
        this.instance.setBlock(position, groundBlock);
    }
}
