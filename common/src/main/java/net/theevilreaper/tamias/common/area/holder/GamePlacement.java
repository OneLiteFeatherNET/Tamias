package net.theevilreaper.tamias.common.area.holder;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.common.area.PlayingArea;
import net.theevilreaper.tamias.common.area.placement.AreaPlacement;
import net.theevilreaper.tamias.common.area.placement.CircleAreaPlacement;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class GamePlacement implements Placement {

    private static final Logger LOGGER = LoggerFactory.getLogger(GamePlacement.class);

    private final AreaPlacement placement;
    private final PlayingArea area;
    private final Instance instance;
    private final Supplier<GroundData> groundDataSupplier;

    public GamePlacement(@NotNull Instance instance, @NotNull PlayingArea area, @NotNull Supplier<GroundData> groundDataSupplier) {
        this.instance = instance;
        this.area = area;
        this.placement = new CircleAreaPlacement<>(
                new ArrayList<>(area.getPositions()),
                () -> new ArrayList<>(area.getTntPositions()),
                this::placeAtPos
        );
        this.groundDataSupplier = groundDataSupplier;
    }

    @Override
    public void clear() {
        clearSet(this.area.getPositions());
        clearSet(this.area.getTntPositions());
        clearSet(this.area.getSpecialPositions());
        this.area.reset();
    }

    private <T extends Point> void clearSet(@NotNull Set<T> positions) {
        for (T position : positions) {
            instance.setBlock(position, Block.AIR);
        }
    }

    private <T extends Point> void spawnTnt(@NotNull T pos) {
        System.out.println("B");
        Entity tntEntity = new Entity(EntityType.FALLING_BLOCK);
        FallingBlockMeta fallingBlockMeta = (FallingBlockMeta) tntEntity.getEntityMeta();
        fallingBlockMeta.setBlock(Block.TNT);
        System.out.println("C");

        // Add random offsets to prevent stacking
        double offsetX = ThreadLocalRandom.current().nextDouble(0.3, 0.7);
        double offsetZ = ThreadLocalRandom.current().nextDouble(0.3, 0.7);

        Pos entityPos = new Pos(pos.x() + offsetX, pos.y() + 0.5, pos.z() + offsetZ);
        tntEntity.setInstance(instance, entityPos);

        System.out.println("entityPos: " + entityPos);

        // Schedule a check to see if the entity is still falling
        tntEntity.scheduler().buildTask(() -> checkIfStillFalling(tntEntity, pos))
                .delay(Duration.ofMillis(500))
                .schedule();
    }

    private <T extends Point> void checkIfStillFalling(@NotNull Entity entity, @NotNull T originalPos) {
        System.out.println("A");
        if (!entity.isOnGround()) {
            // Schedule with a longer delay to reduce resource usage
            entity.scheduler().buildTask(() -> checkIfStillFalling(entity, originalPos))
                    .delay(Duration.ofMillis(100))
                    .schedule();
        } else {
            // Entity has landed, place TNT at the original position
            instance.setBlock(originalPos, Block.TNT);
            System.out.println("TNT landed at " + originalPos);
            entity.remove();
        }
    }

    @Override
    public void triggerPlacement() {
        if (this.placement.isRunning()) return;
        this.placement.place();
    }

    /**
     * Places a block at the specified position.
     * This method is used by the AreaPlacement.
     *
     * @param pos the position to place the block at
     */
    private <T extends Point> void placeAtPos(@NotNull T pos) {
        Set<Point> specialBlocks = this.area.getSpecialPositions();

        GroundData groundData = this.groundDataSupplier.get();
        if (specialBlocks.contains(pos)) {
            instance.setBlock(pos, groundData.getAddtionalBlock());
        } else {
            instance.setBlock(pos, groundData.groundBlock());
        }

        if (ThreadLocalRandom.current().nextInt(0, 100) <= 15) {
            System.out.println("M");// TNT_SPAWN_CHANCE
            spawnTnt(pos);
        }
    }

    /**
     * Applies the calculated positions to the given instance.
     * This method should be called after the instance is available if it wasn't provided in the constructor.
     *
     * @param instance the instance to apply the positions to
     */
    public void applyPositions(@NotNull Instance instance) {
        var start = area.getGameAreaData().lowerCorner();
        var end = area.getGameAreaData().upperCorner();
        Set<Point> areaPositions = area.getPositions();

        // Simplify coordinate handling
        int startBlockX = Math.min(start.blockX(), end.blockX());
        int endBlockX = Math.max(start.blockX(), end.blockX());
        int startBlockZ = Math.min(start.blockZ(), end.blockZ());
        int endBlockZ = Math.max(start.blockZ(), end.blockZ());

        // Preload chunks in batch for better performance
        preloadChunks(instance, startBlockX, endBlockX, startBlockZ, endBlockZ);

        for (Point pos : areaPositions) {
            instance.setBlock(pos, Block.BARRIER);
        }

        replaceCornerBlock(start);
        replaceCornerBlock(end);
        LOGGER.info("Applied {} positions to the instance", areaPositions.size());
    }

    /**
     * Replaces the corner block at the specified position with air.
     *
     * @param position the position to replace the block at
     */
    private void replaceCornerBlock(@NotNull Point position) {
        Block block = instance.getBlock(position);
        if (block == Block.AIR || block == Block.BARRIER) return;
        instance.setBlock(position, Block.AIR);
    }

    /**
     * Preloads chunks in the specified area to improve performance.
     *
     * @param instance the instance to preload chunks in
     * @param startX   the start X coordinate
     * @param endX     the end X coordinate
     * @param startZ   the start Z coordinate
     * @param endZ     the end Z coordinate
     */
    private void preloadChunks(@NotNull Instance instance, int startX, int endX, int startZ, int endZ) {
        // Calculate chunk coordinates
        int startChunkX = startX >> 4;
        int endChunkX = endX >> 4;
        int startChunkZ = startZ >> 4;
        int endChunkZ = endZ >> 4;

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
}
