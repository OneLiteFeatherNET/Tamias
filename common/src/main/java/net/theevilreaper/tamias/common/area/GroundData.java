package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("java:S3252")
public record GroundData(@NotNull Block groundBlock, @Nullable List<Block> additionalBlocks) {

    /**
     * Performs some checks on the given data to avoid invalid states.
     *
     * @param groundBlock      the ground block
     * @param additionalBlocks the additional blocks
     */
    public GroundData {
        Check.argCondition(groundBlock.isAir(), "The ground block can't be air");
        if (additionalBlocks != null) {
            Check.argCondition(additionalBlocks.contains(Block.AIR), "The additional blocks can't contain air");
        }
    }

    /**
     * Checks if the ground data has additional blocks.
     *
     * @return {@code true} if the ground data has additional blocks
     */
    public boolean hasAdditionalBlocks() {
        return additionalBlocks != null && !additionalBlocks.isEmpty();
    }
}
