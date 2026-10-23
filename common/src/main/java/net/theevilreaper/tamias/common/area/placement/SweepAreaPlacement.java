package net.theevilreaper.tamias.common.area.placement;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSoundType;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.ground.GroundData;

import net.minestom.server.timer.TaskSchedule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.GROUND_SOUND_INTERVAL;

/**
 * Places ground blocks in the order defined by the given {@link Comparator}, batched over ticks.
 * The fill order is fully pluggable - see {@link net.theevilreaper.tamias.common.area.GroundFillPattern}
 * for the available patterns - the batching/tracking logic here stays the same for all of them.
 */
public final class SweepAreaPlacement extends AreaBasePlacement {

    private final Set<Vec> specialPositions;
    private final Comparator<Vec> fillOrder;

    public SweepAreaPlacement(
            Instance instance,
            List<Vec> blockPositions,
            List<Vec> specialPositions,
            Comparator<Vec> fillOrder) {
        super(instance, blockPositions);
        this.specialPositions = ConcurrentHashMap.newKeySet();
        this.specialPositions.addAll(specialPositions);
        this.fillOrder = fillOrder;
    }

    /**
     * Replaces the positions which get an additional block of the ground data instead of the ground block.
     *
     * @param positions the new special positions
     */
    public void updateSpecialPositions(Collection<Vec> positions) {
        this.specialPositions.clear();
        this.specialPositions.addAll(positions);
    }

    /**
     * Returns the order in which the ground blocks are placed.
     *
     * @return the fill order
     */
    public Comparator<Vec> getFillOrder() {
        return this.fillOrder;
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
        int[] steps = new int[1];

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
            // One sound per step would be too noisy, so only every few steps one is played at the building front
            if (steps[0]++ % GROUND_SOUND_INTERVAL == 0) {
                playPlaceSound(positions.getLast(), groundData.groundBlock());
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

    /**
     * Plays the place sound of the given block at the given position.
     *
     * @param position the position of the placed block
     * @param block    the placed block
     */
    private void playPlaceSound(Vec position, Block block) {
        BlockSoundType soundType = block.registry().getBlockSoundType();
        if (soundType == null) return;
        Point center = position.add(0.5);
        Sound sound = Sound.sound(soundType.placeSound(), Sound.Source.BLOCK, (soundType.volume() + 1f) / 2f, soundType.pitch() * 0.8f);
        this.instance.playSound(sound, center.x(), center.y(), center.z());
    }
}
