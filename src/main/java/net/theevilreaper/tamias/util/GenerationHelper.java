package net.theevilreaper.tamias.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.theevilreaper.tamias.config.GameConfig;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class GenerationHelper {

    private GenerationHelper() { }

    public static void placeSpawnBlocks(@NotNull Instance instance, Pos... positions) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == null) continue;
            instance.setBlock(positions[i], GameConfig.SPAWN_BLOCK);
        }
    }

    public static void cleanUpBlocks(@NotNull Instance instance, Pos... positions) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == null) continue;
            instance.setBlock(positions[i], Block.AIR);
        }
    }
}
