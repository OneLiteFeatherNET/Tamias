package net.theevilreaper.tamias.setup.util;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;

/**
 * Utility class to display the blocks of a ground preset in an inventory.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class GroundBlocks {

    private GroundBlocks() {
        // Nothing to do here
    }

    /**
     * Returns the material which is used to display the given block in an inventory.
     *
     * @param block the block to display
     * @return the material of the block or a barrier if the block has no item
     */
    public static Material displayMaterial(Block block) {
        Material material = block.material();
        return material == null ? Material.BARRIER : material;
    }

    /**
     * Returns a readable name of the given block, for example {@code Red Sand} for {@code minecraft:red_sand}.
     * Blocks which are switched on get an {@code (On)} suffix, because they share the item with the switched off block.
     *
     * @param block the block to get the name from
     * @return the name of the block
     */
    public static String displayName(Block block) {
        String[] parts = block.key().value().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        if ("true".equals(block.getProperty("lit"))) {
            builder.append(" (On)");
        }
        return builder.toString();
    }
}
