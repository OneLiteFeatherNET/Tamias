package net.theevilreaper.tamias.common.area.placement;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSoundType;
import net.theevilreaper.tamias.common.area.GroundFillPattern;
import net.theevilreaper.tamias.common.event.AreaBuildProgressEvent;
import net.theevilreaper.tamias.common.ground.GroundData;

import net.minestom.server.timer.TaskSchedule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.ToDoubleFunction;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.BLOCKS_PER_STEP;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.GROUND_SOUND_INTERVAL;

/**
 * Places ground blocks batched over ticks. The fill order is passed per run, so one placement serves every
 * {@link net.theevilreaper.tamias.common.area.GroundFillPattern} - the batching/tracking logic stays the same for all of them.
 * After each step an {@link AreaBuildProgressEvent} is called, so the game decides how to show the progress.
 * The end of a run is reported by the future which {@link #place(GroundData, ToDoubleFunction)} returns.
 */
public final class SweepAreaPlacement extends AreaBasePlacement {

    private final List<Vec> blockPositions;
    private final Set<Vec> specialPositions;
    private CompletableFuture<Void> built = CompletableFuture.completedFuture(null);

    /**
     * Creates a new placement.
     *
     * @param instance         the instance to place the blocks in
     * @param blockPositions   the positions of the ground
     * @param specialPositions the positions which get an additional block of the ground data
     */
    public SweepAreaPlacement(Instance instance, List<Vec> blockPositions, List<Vec> specialPositions) {
        super(instance);
        this.blockPositions = new ArrayList<>(blockPositions);
        this.specialPositions = ConcurrentHashMap.newKeySet();
        this.specialPositions.addAll(specialPositions);
    }

    /**
     * Replaces the positions of the ground.
     * Used to refresh the placement once the area has been scanned against the live instance,
     * since the positions supplied at construction time may not yet reflect the actual terrain.
     *
     * @param positions the new positions of the ground
     */
    public void updatePositions(List<Vec> positions) {
        this.blockPositions.clear();
        this.blockPositions.addAll(positions);
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
     * Starts the placement of the ground with the given data.
     *
     * @param groundData the data which defines the ground and the special blocks
     * @param fillOrder  the key per position which defines the order of the ground blocks, a lower key is placed earlier
     * @return a future which completes once every ground block is placed, the future of the running run if one is
     * already running; it doesn't complete when the run is stopped before
     */
    public CompletableFuture<Void> place(GroundData groundData, ToDoubleFunction<? super Vec> fillOrder) {
        if (!tryStart()) return this.built;
        CompletableFuture<Void> run = new CompletableFuture<>();
        this.built = run;
        List<Vec> posList = GroundFillPattern.sort(this.blockPositions, fillOrder);
        int total = posList.size();
        LinkedBlockingDeque<Vec> queue = new LinkedBlockingDeque<>(posList);
        int[] steps = new int[1];

        this.buildTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<Vec> positions = new ArrayList<>();
            for (int i = 0; !queue.isEmpty() && i < BLOCKS_PER_STEP; i++) {
                positions.add(queue.poll());
            }
            if (positions.isEmpty()) {
                finish();
                run.complete(null);
                return;
            }
            for (Vec pos : positions) {
                this.doPlaceBlock(pos, getBlock(pos, groundData));
            }
            // One sound per step would be too noisy, so only every few steps one is played at the building front
            if (steps[0]++ % GROUND_SOUND_INTERVAL == 0) {
                playPlaceSound(positions.getLast(), groundData.groundBlock());
            }
            EventDispatcher.call(new AreaBuildProgressEvent(1 - (double) queue.size() / total));
        }).repeat(TaskSchedule.tick(1)).schedule();
        return run;
    }

    /**
     * Returns the future of the current run.
     *
     * @return a future which completes once every ground block of the current run is placed, a completed one if no
     * run was started
     */
    public CompletableFuture<Void> built() {
        return this.built;
    }

    /**
     * Returns the block for the given position, special positions get an additional block of the ground data.
     *
     * @param position   the position of the block
     * @param groundData the data which defines the ground and the special blocks
     * @return the block to place
     */
    private Block getBlock(Vec position, GroundData groundData) {
        if (this.specialPositions.contains(position)) {
            return groundData.getAdditionalBlock();
        }
        return groundData.groundBlock();
    }

    /**
     * Plays the place sound of the given block at the given position.
     *
     * @param position the position of the placed block
     * @param block    the placed block
     */
    private void playPlaceSound(Vec position, Block block) {
        BlockSoundType soundType = block.blockSoundType();
        if (soundType == null) return;
        Point center = position.add(0.5);
        Sound sound = Sound.sound(soundType.placeSound(), Sound.Source.BLOCK, (soundType.volume() + 1f) / 2f, soundType.pitch() * 0.8f);
        this.instance.playSound(sound, center.x(), center.y(), center.z());
    }
}
