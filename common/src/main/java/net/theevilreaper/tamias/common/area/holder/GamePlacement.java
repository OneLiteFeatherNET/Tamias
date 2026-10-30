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
import net.theevilreaper.tamias.common.area.placement.SupplyArc;
import net.theevilreaper.tamias.common.area.placement.SupplyRing;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public final class GamePlacement implements Placement {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamePlacement.class);

    private final Map<GroundFillPattern, SweepAreaPlacement> groundPatterns;
    private final TNTPlacement tntPlacement;
    private final GameArea area;
    private final Instance instance;
    private SweepAreaPlacement activePattern;
    private List<Vec> tntCandidates;
    private Set<Vec> supplyCandidates;
    private @Nullable GroundData groundData;
    private boolean prepared;

    public GamePlacement(Instance instance, GameArea area) {
        this.instance = instance;
        this.area = area;
        this.tntPlacement = new TNTPlacement(instance, area::addTntPosition, this::placeRing);
        this.tntCandidates = List.of();
        this.supplyCandidates = Set.of();

        List<Vec> initialPositions = List.copyOf(area.getPositions());
        Vec center = getCenter(area);
        this.groundPatterns = new EnumMap<>(GroundFillPattern.class);
        for (GroundFillPattern pattern : GroundFillPattern.values()) {
            this.groundPatterns.put(pattern, new SweepAreaPlacement(instance, initialPositions, List.of(), pattern.comparator(center)));
        }
        this.activePattern = this.groundPatterns.get(GroundFillPattern.values()[0]);
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
        this.groundPatterns.values().forEach(SweepAreaPlacement::stop);
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
        if (this.activePattern.isRunning()) return;
        GroundFillPattern[] patterns = GroundFillPattern.values();
        GroundFillPattern chosen = patterns[ThreadLocalRandom.current().nextInt(patterns.length)];
        this.activePattern = this.groundPatterns.get(chosen);
        LOGGER.info("Using ground fill pattern {}", chosen);
        this.groundData = groundData;
        this.updateSpecialPositions(groundData);
        this.activePattern.place(groundData);
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
        for (SweepAreaPlacement pattern : this.groundPatterns.values()) {
            pattern.updatePositions(filteredPositions);
        }
        // The structures on the map are static, so the free drop paths only have to be found once
        this.tntCandidates = filteredPositions.stream()
                .map(pos -> pos.add(0, 1, 0))
                .filter(pos -> isDropPathFree(pos, GameAreaHelper.TNT_DROP_HEIGHT))
                .toList();
        this.supplyCandidates = this.tntCandidates.stream()
                .filter(pos -> isDropPathFree(pos, GameAreaHelper.SUPPLY_DROP_HEIGHT))
                .collect(Collectors.toUnmodifiableSet());
        LOGGER.info("Found {} TNT candidates with a free drop path", this.tntCandidates.size());
        LOGGER.info("Found {} TNT candidates for a supply drop", this.supplyCandidates.size());
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
        return this.activePattern.isRunning();
    }

    /**
     * Drops a random amount of TNT onto the ground area. A part of the TNT is dropped in groups, see
     * {@link TntLayout}. The groups are dropped in the order of the active fill pattern and each group waits until
     * the ground below it is placed, so the TNT follows the ground placement. A TNT block only becomes a respawn
     * ticket once it has landed.
     * Some rounds get supply drops, see {@link GameAreaHelper#SUPPLY_DROP_CHANCE}.
     *
     * @param countSupplier provides the amount of TNT blocks to drop
     * @return a future which completes once every TNT block has landed
     */
    public CompletableFuture<Void> dropTnt(IntSupplier countSupplier) {
        RandomGenerator random = ThreadLocalRandom.current();
        int supplyDrops = random.nextDouble() < GameAreaHelper.SUPPLY_DROP_CHANCE
                ? random.nextInt(GameAreaHelper.MIN_SUPPLY_DROPS, GameAreaHelper.MAX_SUPPLY_DROPS + 1)
                : 0;
        return dropTnt(countSupplier, supplyDrops, random);
    }

    /**
     * Drops a random amount of TNT onto the ground area with the given amount of supply drops.
     *
     * @param countSupplier provides the amount of TNT blocks to drop
     * @param supplyDrops   the amount of supply drops, fewer are dropped if no position fits
     * @param random        the random generator to plan the TNT with
     * @return a future which completes once every TNT block has landed
     */
    CompletableFuture<Void> dropTnt(IntSupplier countSupplier, int supplyDrops, RandomGenerator random) {
        if (this.tntCandidates.isEmpty()) return CompletableFuture.completedFuture(null);

        SweepAreaPlacement pattern = this.activePattern;
        // Without an impact block no ring is placed, so a supply drop doesn't have to wait for the ground of a ring
        boolean withRing = this.groundData != null && this.groundData.hasImpactBlock();
        List<TntGroup> groups = new ArrayList<>();
        for (TntGroup group : TntLayout.plan(
                this.tntCandidates,
                countSupplier.getAsInt(),
                GameAreaHelper.TNT_GROUP_RATIO,
                supplyDrops,
                this.supplyCandidates::contains,
                (anchor, member) -> SupplyArc.isFree(anchor, member, position -> this.instance.getBlock(position).air()),
                random
        )) {
            groups.add(group.isSupply() && withRing ? group.withRing(SupplyRing.positions(group.center().sub(0, 1, 0), this.area.getPositions())) : group);
        }
        groups.sort(Comparator.comparing(TntGroup::center, pattern.getFillOrder()));

        this.area.setTntPositions(List.of());
        this.tntPlacement.updateGroups(groups, pattern::isPlaced);
        this.tntPlacement.place();
        LOGGER.info("Dropping {} TNT blocks in {} groups with {} supply drops onto the game area",
                groups.stream().mapToInt(group -> group.members().size() + 1).sum(), groups.size(),
                groups.stream().filter(TntGroup::isSupply).count());
        return this.tntPlacement.landed();
    }

    /**
     * Places the ring of a supply drop into the ground with the impact block of the current ground data.
     * Special positions keep their additional block.
     *
     * @param group the supply drop which has landed
     */
    private void placeRing(TntGroup group) {
        GroundData currentData = this.groundData;
        if (currentData == null || !currentData.hasImpactBlock()) return;
        Block impactBlock = currentData.impactBlock();
        Set<Vec> specialPositions = this.area.getSpecialPositions();
        for (Vec position : group.ring()) {
            if (specialPositions.contains(position)) continue;
            this.instance.setBlock(position, impactBlock);
        }
    }

    /**
     * Checks if a TNT block can fall from the given height above the target down to it.
     *
     * @param target the position of the TNT block
     * @param height the height above the target from which the TNT block falls
     * @return true if every block of the drop path is air
     */
    private boolean isDropPathFree(Vec target, int height) {
        for (int y = 0; y <= height; y++) {
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
        this.activePattern.updateSpecialPositions(this.area.getSpecialPositions());
    }
}
