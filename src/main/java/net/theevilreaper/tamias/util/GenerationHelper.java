package net.theevilreaper.tamias.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.config.GameConfig;
import org.jetbrains.annotations.NotNull;

/**
 * The generation helper provides some methods to place and clean up blocks at specific positions.
 * This class is a utility class and cannot be instantiated.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class GenerationHelper {

    private GenerationHelper() {
    }

    /**
     * Places the spawn blocks at the given positions
     *
     * @param instance  the instance to place the blocks
     * @param positions the positions to place the blocks
     */
    public static void placeSpawnBlocks(@NotNull Instance instance, Pos @NotNull ... positions) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == null) continue;
            instance.setBlock(positions[i], GameConfig.SPAWN_BLOCK);
        }
    }

    /**
     * Cleans up the blocks at the given positions
     *
     * @param instance  the instance to clean up the blocks
     * @param positions the positions to clean up
     */
    public static void cleanUpBlocks(@NotNull Instance instance, Pos @NotNull ... positions) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == null) continue;
            instance.setBlock(positions[i], Block.AIR);
        }
    }
}
