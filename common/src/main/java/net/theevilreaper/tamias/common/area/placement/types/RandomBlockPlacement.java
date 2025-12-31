package net.theevilreaper.tamias.common.area.placement.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.placement.AreaBasePlacement;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.ground.GroundData;

import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;

public final class RandomBlockPlacement extends AreaBasePlacement<Vec> {

    private final List<Vec> specialPositions;

    public RandomBlockPlacement(
            Instance instance,
            List<Vec> blockPositions,
            List<Vec> specialPositions) {
        super(instance, blockPositions);
        this.specialPositions = specialPositions;
    }

    @Override
    public void place(GroundData groundData) {
        if (this.buildTask != null) return;

        List<Vec> shuffledBlocks = new ArrayList<>(this.blockPositions);
        Collections.shuffle(shuffledBlocks);
        Deque<Vec> queue = new ArrayDeque<>(shuffledBlocks);

        this.buildTask = MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    for (int i = 0; i < BLOCKS_PER_STEP && !queue.isEmpty(); i++) {
                        Vec pos = queue.pollFirst();
                        placeBlock(pos, groundData);
                    }
                    if (queue.isEmpty()) {
                        buildTask.cancel();
                        buildTask = null;
                        EventDispatcher.call(new AreaFinishBuildEvent());
                    } else {
                        sendExpCount(queue.size());
                    }
                })
                .repeat(5, ChronoUnit.MILLIS)
                .schedule();
    }

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
