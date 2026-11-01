package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import net.theevilreaper.tamias.common.area.GroundFillPattern;
import net.theevilreaper.tamias.common.area.placement.SweepAreaPlacement;
import net.theevilreaper.tamias.common.area.placement.TNTPlacement;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import net.theevilreaper.tamias.common.area.placement.TntLayout;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import java.util.function.ToDoubleFunction;

public final class GamePlacement implements Placement {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamePlacement.class);

    private final SweepAreaPlacement groundPlacement;
    private final TNTPlacement tntPlacement;
    private final GameArea area;
    private final Instance instance;
    private final Vec center;
    private ToDoubleFunction<Point> activeFillOrder;
    private @Nullable GroundFillPattern lastPattern;
    private List<Vec> tntCandidates;
    private boolean prepared;

    public GamePlacement(Instance instance, GameArea area) {
        this.instance = instance;
        this.area = area;
        this.tntPlacement = new TNTPlacement(instance, area::addTntPosition);
        this.tntCandidates = List.of();
        this.center = getCenter(area);
        this.groundPlacement = new SweepAreaPlacement(instance, List.copyOf(area.getPositions()), List.of());
        this.activeFillOrder = GroundFillPattern.CIRCLE.order(GroundFillPattern.Context.of(this.center));
    }

    /**
     * Returns the center between the two corners of the given area.
     *
     * @param area the area to get the center from
     * @return the center of the area
     */
    private static Vec getCenter(GameArea area) {
        AreaData areaData = area.getGameAreaData();
        return areaData.lowerCorner().add(areaData.upperCorner()).div(2);
    }

    @Override
    public void clear() {
        this.clearAsync();
    }

    /**
     * Stops the TNT placement and all ground patterns without removing any block.
     * The blocks which are already placed stay in the instance, the TNT which has not landed yet is cancelled.
     */
    public void stopPlacements() {
        this.tntPlacement.stop();
        this.groundPlacement.stop();
    }

    /**
     * Stops all running placements and removes the ground and the TNT from the instance.
     * Every position is only cleared once and all blocks are set with a single {@link AbsoluteBlockBatch}, so the
     * players receive one update per chunk instead of one per block.
     *
     * @return a future which completes in the instance tick after all blocks are removed
     */
    public CompletableFuture<Void> clearAsync() {
        stopPlacements();

        // The placed ground positions and the special positions are part of the area positions
        Set<Point> positions = new HashSet<>(this.area.getPositions());
        positions.addAll(this.tntPlacement.getPlacedPositions());
        positions.addAll(this.area.getTntPositions());
        this.area.reset();

        AbsoluteBlockBatch batch = new AbsoluteBlockBatch();
        for (Point position : positions) {
            batch.setBlock(position, Block.AIR);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        batch.apply(this.instance, _ -> future.complete(null));
        return future;
    }

    @Override
    public void triggerPlacement(GroundData groundData) {
        if (this.groundPlacement.isRunning()) return;
        GroundFillPattern chosen = GroundFillPattern.next(this.lastPattern, ThreadLocalRandom.current());
        this.lastPattern = chosen;
        this.activeFillOrder = chosen.order(
                new GroundFillPattern.Context(this.center, this.area.getPositions(), ThreadLocalRandom.current())
        );
        LOGGER.info("Using ground fill pattern {}", chosen);
        this.updateSpecialPositions(groundData);
        this.groundPlacement.place(groundData, this.activeFillOrder);
    }

    /**
     * Prepares the game area once: keeps only the marked ground positions and replaces them with barriers.
     * Further calls are ignored, a second flatten would treat the barriers as foreign blocks and remove the whole ground.
     */
    public void prepare() {
        if (this.prepared) return;
        this.prepared = true;
        flatten();
        applyPositions();
    }

    /**
     * Returns whether the game area is already prepared.
     *
     * @return true if {@link #prepare()} was called before
     */
    public boolean isPrepared() {
        return this.prepared;
    }

    void flatten() {
        Set<Vec> positionsToRemove = new HashSet<>();
        replaceCornerBlock(this.area.getGameAreaData().lowerCorner());
        replaceCornerBlock(this.area.getGameAreaData().upperCorner());
        for (Vec pos : area.getPositions()) {
            Block block = instance.getBlock(pos);
            if (block != GameAreaHelper.GROUND_MARKER_BLOCK) {
                positionsToRemove.add(pos);
            }
        }
        this.area.flattenPositions(positionsToRemove);
        List<Vec> filteredPositions = List.copyOf(this.area.getPositions());
        this.groundPlacement.updatePositions(filteredPositions);
        // The structures on the map are static, so the free drop paths only have to be found once
        this.tntCandidates = filteredPositions.stream()
                .map(pos -> pos.add(0, 1, 0))
                .filter(this::isDropPathFree)
                .toList();
        LOGGER.info("Found {} TNT candidates with a free drop path", this.tntCandidates.size());
        LOGGER.info("Flatten area by {} positions, {} remain as ground candidates", positionsToRemove.size(), this.area.getPositions().size());
    }

    /**
     * Applies the calculated positions to the given instance.
     * This method should be called after the instance is available if it wasn't provided in the constructor.
     */
    void applyPositions() {
        Collection<Vec> areaPositions = area.getPositions();

        for (Vec pos : areaPositions) {
            instance.setBlock(pos, Block.BARRIER);
        }
        LOGGER.info("Applied {} positions to the instance", areaPositions.size());
    }

    /**
     * Replaces the corner block at the specified position with air.
     *
     * @param position the position to replace the block at
     */
    private void replaceCornerBlock(Point position) {
        Block block = instance.getBlock(position);
        if (block == Block.AIR || block == Block.BARRIER) return;
        instance.setBlock(position, Block.AIR);
    }

    /**
     * Preloads chunks in the specified area to improve performance.
     * The returned future completes once every chunk in the area's bounding box has loaded;
     * it does not block the calling thread.
     */
    public CompletableFuture<Void> preloadChunks() {
        var start = area.getGameAreaData().lowerCorner();
        var end = area.getGameAreaData().upperCorner();

        int startBlockX = Math.min(start.blockX(), end.blockX());
        int endBlockX = Math.max(start.blockX(), end.blockX());
        int startBlockZ = Math.min(start.blockZ(), end.blockZ());
        int endBlockZ = Math.max(start.blockZ(), end.blockZ());

        int startChunkX = startBlockX >> 4;
        int endChunkX = endBlockX >> 4;
        int startChunkZ = startBlockZ >> 4;
        int endChunkZ = endBlockZ >> 4;

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
            for (int chunkZ = startChunkZ; chunkZ <= endChunkZ; chunkZ++) {
                futures.add(instance.loadChunk(chunkX, chunkZ));
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public boolean isRunning() {
        return this.groundPlacement.isRunning();
    }

    /**
     * Drops a random amount of TNT onto the ground area. A part of the TNT is dropped in groups, see
     * {@link TntLayout}. The groups are dropped in the order of the active fill pattern and each group waits until
     * the ground below it is placed, so the TNT follows the ground placement. A TNT block only becomes a respawn
     * ticket once it has landed.
     *
     * @param countSupplier provides the amount of TNT blocks to drop
     * @return a future which completes once every TNT block has landed
     */
    public CompletableFuture<Void> dropTnt(IntSupplier countSupplier) {
        if (this.tntCandidates.isEmpty()) return CompletableFuture.completedFuture(null);

        List<TntGroup> groups = new ArrayList<>(TntLayout.plan(
                this.tntCandidates,
                countSupplier.getAsInt(),
                GameAreaHelper.TNT_GROUP_RATIO,
                ThreadLocalRandom.current()
        ));
        ToDoubleFunction<Point> fillOrder = this.activeFillOrder;
        groups.sort(Comparator.comparingDouble(group -> fillOrder.applyAsDouble(group.center())));

        this.area.setTntPositions(List.of());
        this.tntPlacement.updateGroups(groups, this.groundPlacement::isPlaced);
        this.tntPlacement.place();
        LOGGER.info("Dropping {} TNT blocks in {} groups onto the game area",
                groups.stream().mapToInt(group -> group.members().size() + 1).sum(), groups.size());
        return this.tntPlacement.landed();
    }

    /**
     * Checks if a TNT block can fall from {@link GameAreaHelper#TNT_DROP_HEIGHT} blocks above the target down to it.
     *
     * @param target the position of the TNT block
     * @return true if every block of the drop path is air
     */
    private boolean isDropPathFree(Vec target) {
        for (int y = 0; y <= GameAreaHelper.TNT_DROP_HEIGHT; y++) {
            if (!instance.getBlock(target.add(0, y, 0)).registry().isAir()) return false;
        }
        return true;
    }

    /**
     * Picks new special positions for the given ground data, they get an additional block instead of the ground
     * block. Nothing is picked if the ground data has no additional blocks.
     *
     * @param groundData the ground data which is placed next
     */
    private void updateSpecialPositions(GroundData groundData) {
        if (groundData.hasAdditionalBlocks()) {
            int positions = this.area.getPositions().size();
            int specialBlocks = Math.ceilDiv(positions, GameAreaHelper.POSITIONS_PER_SPECIAL_BLOCK);
            this.area.calculateSpecialBlockPositions(() -> specialBlocks);
        } else {
            this.area.getSpecialPositions().clear();
        }
        this.groundPlacement.updateSpecialPositions(this.area.getSpecialPositions());
    }
}
