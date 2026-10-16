package net.theevilreaper.tamias.setup.util;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class which decides which blocks can be used in a ground preset.
 * The ground marker and tnt are reserved by the game logic, and only blocks with a full top face are allowed
 * because players have to stand on them.
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
     * Returns the block of the given item if it can be used in a ground preset.
     *
     * @param stack the item to convert
     * @return the block or {@code null} if the item is no valid ground block
     */
    public static @Nullable Block fromItem(ItemStack stack) {
        if (stack.isAir()) return null;
        Block block = stack.material().block();
        if (block == null || !isAllowed(block)) return null;
        return block;
    }

    /**
     * Checks if the given block can be used in a ground preset.
     *
     * @param block the block to check
     * @return {@code true} if the block is allowed
     */
    public static boolean isAllowed(Block block) {
        if (block.air() || block.liquid() || !block.solid()) return false;
        if (block.compare(GameAreaHelper.GROUND_MARKER_BLOCK) || block.compare(Block.TNT)) return false;
        return block.collisionShape().isFaceFull(BlockFace.TOP);
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
     * Returns a readable name of the given block.
     *
     * @param block the block to get the name from
     * @return the name of the block
     */
    public static String displayName(Block block) {
        return block.key().value();
    }
}
