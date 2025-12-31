package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.PlayingArea;
import net.theevilreaper.tamias.common.area.placement.AreaPlacement;
import net.theevilreaper.tamias.common.area.placement.types.RandomBlockPlacement;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GamePlacement implements Placement {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamePlacement.class);

    private final AreaPlacement placement;
    private final PlayingArea area;
    private final Instance instance;

    public GamePlacement(Instance instance, PlayingArea area) {
        this.instance = instance;
        this.area = area;
        this.placement = new RandomBlockPlacement(
                this.instance,
                this.area.getPositions().stream().map(it -> (Vec) it).toList(),
                new ArrayList<>(this.area.getSpecialPositions())
        );
    }

    @Override
    public void clear() {
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
        if (this.placement.isRunning()) return;
        this.placement.place(groundData);
    }

    public void flatten() {
        Set<Vec> positions = new HashSet<>();
        replaceCornerBlock(this.area.getGameAreaData().lowerCorner());
        replaceCornerBlock(this.area.getGameAreaData().upperCorner());
        for (Point pos : area.getPositions()) {
            Block block = instance.getBlock(pos);
            if (block == Block.AIR) {
                positions.add(((Vec) pos));
                continue;
            }

            if (instance.getBlock(pos.add(0, 1,0)) != Block.AIR) {
                positions.add(((Vec) pos));
            }
        }
        ((GameArea) this.area).flattenPositions(positions);
        LOGGER.info("Flatten area by {} positions", positions.size());
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
     *
     */
    public void preloadChunks() {
        var start = area.getGameAreaData().lowerCorner();
        var end = area.getGameAreaData().upperCorner();

        // Simplify coordinate handling
        int startBlockX = Math.min(start.blockX(), end.blockX());
        int endBlockX = Math.max(start.blockX(), end.blockX());
        int startBlockZ = Math.min(start.blockZ(), end.blockZ());
        int endBlockZ = Math.max(start.blockZ(), end.blockZ());


        // Calculate chunk coordinates
        int startChunkX = startBlockX >> 4;
        int endChunkX = endBlockX >> 4;
        int startChunkZ = startBlockZ >> 4;
        int endChunkZ = endBlockZ >> 4;

        // Create a list of chunk positions to load
        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (int chunkX = startChunkX; chunkX <= endChunkX; chunkX++) {
            for (int chunkZ = startChunkZ; chunkZ <= endChunkZ; chunkZ++) {
                futures.add(instance.loadChunk(chunkX, chunkZ));
            }
        }

        // Wait for all chunks to load
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    public boolean isRunning() {
        return this.placement.isRunning();
    }
}
