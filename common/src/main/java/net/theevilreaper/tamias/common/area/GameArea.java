package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

/**
 * The GameArea class represents a playable area in the Minecraft world.
 * It handles the calculation of positions, placement of blocks, and spawning of TNT entities.
 * This implementation focuses on performance optimization and thread safety.
 * The instance can be null during initialization and set later.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class GameArea implements PlayingArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameArea.class);

    private final AreaData areaData;
    private final List<Vec> areaPositions;
    private final Set<Vec> specialBlocks;
    private final Set<Vec> tntPositions;

    public GameArea(AreaData areaData) {
        this.areaData = areaData;
        this.areaPositions = new ArrayList<>();
        this.specialBlocks = new HashSet<>();
        this.tntPositions = new HashSet<>();
    }

    @Override
    public void calculatePositions() {
        if (!this.areaPositions.isEmpty()) {
            throw new IllegalStateException("The calculation only can runs at once");
        }
        var start = areaData.lowerCorner();
        var end = areaData.upperCorner();

        // Simplify coordinate handling
        int startBlockX = Math.min(start.blockX(), end.blockX());
        int endBlockX = Math.max(start.blockX(), end.blockX());
        int startBlockZ = Math.min(start.blockZ(), end.blockZ());
        int endBlockZ = Math.max(start.blockZ(), end.blockZ());
        int blockY = start.blockY();

        // Calculate positions without applying them to the instance yet
        for (int x = startBlockX; x <= endBlockX; x++) {
            for (int z = startBlockZ; z <= endBlockZ; z++) {
                areaPositions.add(new Vec(x, blockY, z));
            }
        }

        LOGGER.info("Calculated {} potential area positions", areaPositions.size());
    }

    public void flattenPositions(Set<Vec> positions) {
        this.areaPositions.removeAll(positions);
    }

    @Override
    public void reset() {
        synchronized (this.tntPositions) {
            this.tntPositions.clear();
        }
        this.specialBlocks.clear();
    }

    @Override
    public @UnmodifiableView Set<Vec> getPositions() {
        Set<Vec> result = new HashSet<>(areaPositions);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Calculates the positions for the special blocks.
     */
    @Override
    public void calculateSpecialBlockPositions(IntSupplier specialBlockCount) {
        this.specialBlocks.clear();
        // Create a copy of the area positions to avoid modifying the original list
        List<Vec> availablePositions = new ArrayList<>(this.areaPositions);
        int count = Math.min(specialBlockCount.getAsInt(), availablePositions.size());

        for (int i = 0; i < count; i++) {
            // Get a random index within the remaining available positions
            int randomIndex = ThreadLocalRandom.current().nextInt(0, availablePositions.size());
            // Add the position to special blocks
            specialBlocks.add(availablePositions.get(randomIndex));
            // Remove the position to avoid duplicates
            availablePositions.remove(randomIndex);
        }

        LOGGER.info("Calculated {} special block positions", this.specialBlocks.size());
    }

    /**
     * Adds a position to the TNT positions, e.g. when a TNT block has landed.
     *
     * @param position the position of the placed TNT block
     */
    public void addTntPosition(Vec position) {
        synchronized (this.tntPositions) {
            this.tntPositions.add(position);
        }
    }

    /**
     * Replaces the tnt positions with the given ones.
     * Each position is the position of a placed TNT block and acts as a respawn ticket for a bomber.
     *
     * @param positions the positions of the placed TNT blocks
     */
    public void setTntPositions(Collection<Vec> positions) {
        synchronized (this.tntPositions) {
            this.tntPositions.clear();
            this.tntPositions.addAll(positions);
        }
    }

    /**
     * Returns a random position from the list of tnt positions.
     * This method is thread-safe.
     *
     * @return a random position or null if the list is empty
     */
    public @Nullable Pos getRandomPosition() {
        synchronized (this.tntPositions) {
            if (this.tntPositions.isEmpty()) return null;

            int skip = ThreadLocalRandom.current().nextInt(this.tntPositions.size());
            Iterator<Vec> iterator = this.tntPositions.iterator();
            for (int i = 0; i < skip; i++) {
                iterator.next();
            }
            Vec position = iterator.next();
            iterator.remove();
            return position.asPos();
        }
    }

    @Override
    public AreaData getGameAreaData() {
        return this.areaData;
    }

    /**
     * Returns a copy of the tnt positions, so the caller can iterate it while TNT is still landing.
     *
     * @return a copy of the positions of the placed TNT blocks
     */
    @Override
    public Set<Vec> getTntPositions() {
        synchronized (this.tntPositions) {
            return new HashSet<>(this.tntPositions);
        }
    }

    @Override
    public Set<Vec> getSpecialPositions() {
        return this.specialBlocks;
    }
}
