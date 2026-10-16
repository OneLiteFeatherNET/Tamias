package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import net.theevilreaper.tamias.common.area.GroundFillPattern;
import net.theevilreaper.tamias.common.area.placement.SweepAreaPlacement;
import net.theevilreaper.tamias.common.area.placement.TNTPlacement;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

public final class GamePlacement implements Placement {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamePlacement.class);

    private final Map<GroundFillPattern, SweepAreaPlacement> groundPatterns;
    private final TNTPlacement tntPlacement;
    private final GameArea area;
    private final Instance instance;
    private SweepAreaPlacement activePattern;
    private List<Vec> tntCandidates;

    public GamePlacement(Instance instance, GameArea area) {
        this.instance = instance;
        this.area = area;
        this.tntPlacement = new TNTPlacement(instance, new ArrayList<>());
        this.tntCandidates = List.of();

        List<Vec> initialPositions = area.getPositions().stream().map(Vec.class::cast).toList();
        this.groundPatterns = new EnumMap<>(GroundFillPattern.class);
        for (GroundFillPattern pattern : GroundFillPattern.values()) {
            this.groundPatterns.put(pattern, new SweepAreaPlacement(instance, initialPositions, List.of(), pattern.comparator()));
        }
        this.activePattern = this.groundPatterns.get(GroundFillPattern.values()[0]);
    }

    @Override
    public void clear() {
        this.tntPlacement.stop();
        clearSet(this.tntPlacement.getPlacedPositions());
        for (SweepAreaPlacement pattern : this.groundPatterns.values()) {
            pattern.stop();
            clearSet(pattern.getPlacedPositions());
        }
        clearSet(this.area.getPositions());
        clearSet(this.area.getTntPositions());
        clearSet(this.area.getSpecialPositions());
        this.area.reset();
    }

    private <T extends Point> void clearSet(Collection<T> positions) {
        for (T position : positions) {
            instance.setBlock(position, Block.AIR);
        }
    }

    @Override
    public void triggerPlacement(GroundData groundData) {
        if (this.activePattern.isRunning()) return;
        GroundFillPattern[] patterns = GroundFillPattern.values();
        GroundFillPattern chosen = patterns[ThreadLocalRandom.current().nextInt(patterns.length)];
        this.activePattern = this.groundPatterns.get(chosen);
        LOGGER.info("Using ground fill pattern {}", chosen);
        this.updateSpecialPositions(groundData);
        this.activePattern.place(groundData);
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

    public void flatten() {
        Set<Vec> positionsToRemove = new HashSet<>();
        replaceCornerBlock(this.area.getGameAreaData().lowerCorner());
        replaceCornerBlock(this.area.getGameAreaData().upperCorner());
        for (Point pos : area.getPositions()) {
            Block block = instance.getBlock(pos);
            if (block != GameAreaHelper.GROUND_MARKER_BLOCK) {
                positionsToRemove.add((Vec) pos);
            }
        }
        this.area.flattenPositions(positionsToRemove);
        List<Vec> filteredPositions = this.area.getPositions().stream().map(Vec.class::cast).toList();
        for (SweepAreaPlacement pattern : this.groundPatterns.values()) {
            pattern.updatePositions(filteredPositions);
        }
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
    public void applyPositions() {
        Collection<Point> areaPositions = area.getPositions();

        for (Point pos : areaPositions) {
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
     * Drops a random amount of TNT onto the ground area.
     * Candidates are picked from the whole known area (not just what the ground placement has
     * placed so far) so the spread covers the entire map even while ground placement - which fills
     * outward from the world origin and is much faster than the staggered TNT fall - is still
     * running. Only positions with a free drop path are used, so TNT never spawns inside or falls
     * through a structure.
     *
     * @param countSupplier supplies how many TNT blocks should be dropped
     */
    public void dropTnt(IntSupplier countSupplier) {
        if (this.tntCandidates.isEmpty()) return;

        List<Vec> candidates = new ArrayList<>(this.tntCandidates);
        Collections.shuffle(candidates);
        int count = Math.min(countSupplier.getAsInt(), candidates.size());
        List<Vec> selected = selectSpreadOut(candidates, count);

        this.area.setTntPositions(selected);
        this.tntPlacement.updatePositions(selected);
        this.tntPlacement.place(GroundDataRegistry.DEFAULT_SPAWN_DATA);
        LOGGER.info("Dropping {} TNT blocks onto the game area", selected.size());
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
     * Greedily picks {@code count} positions out of the (already shuffled) candidates, preferring
     * ones that are spread apart from each other instead of a purely random pick, which tends to
     * clump positions together by chance. The minimum spacing is derived from how dense the
     * candidates are relative to the requested count, and gets relaxed pass by pass until enough
     * positions are found.
     *
     * @param shuffledCandidates the shuffled pool of eligible positions
     * @param count              how many positions to pick
     * @return the picked positions, spread out as far as the candidate pool allows
     */
    private static List<Vec> selectSpreadOut(List<Vec> shuffledCandidates, int count) {
        List<Vec> selected = new ArrayList<>(count);
        double spacing = Math.sqrt((double) shuffledCandidates.size() / count);

        while (selected.size() < count && spacing >= 1.0) {
            double spacingSquared = spacing * spacing;
            for (Vec candidate : shuffledCandidates) {
                if (selected.size() >= count) break;
                if (selected.contains(candidate)) continue;

                boolean farEnough = true;
                for (Vec picked : selected) {
                    if (picked.distanceSquared(candidate) < spacingSquared) {
                        farEnough = false;
                        break;
                    }
                }
                if (farEnough) selected.add(candidate);
            }
            spacing -= 1.0;
        }

        // The candidate pool is too dense for the requested spacing - fill the rest regardless of distance.
        for (Vec candidate : shuffledCandidates) {
            if (selected.size() >= count) break;
            if (!selected.contains(candidate)) selected.add(candidate);
        }

        return selected;
    }
}
