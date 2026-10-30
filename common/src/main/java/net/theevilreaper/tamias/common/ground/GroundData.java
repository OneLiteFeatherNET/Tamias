package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A {@link GroundData} represents the definition of the ground in a specific area.
 * Mainly used to define which is the main block for the ground and if there are additional blocks.
 * If there are additional blocks they will be used to fill some blocks in the area.
 * The impact block is a color variant of the ground block which marks the impact of a supply drop.
 *
 * @param groundBlock      the main block for the ground
 * @param additionalBlocks the additional blocks for the ground
 * @param impactBlock      the block for the ring around the impact of a supply drop
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public record GroundData(Block groundBlock, @Nullable List<Block> additionalBlocks, @Nullable Block impactBlock) {

    /**
     * Performs some checks on the given data to avoid invalid states.
     *
     * @param groundBlock      the ground block
     * @param additionalBlocks the additional blocks
     * @param impactBlock      the impact block
     */
    public GroundData {
        Check.argCondition(groundBlock.air(), "The ground block can't be air");
        if (additionalBlocks != null) {
            Check.argCondition(additionalBlocks.contains(Block.AIR), "The additional blocks can't contain air");
        }
        if (impactBlock != null) {
            Check.argCondition(impactBlock.air(), "The impact block can't be air");
        }
    }

    /**
     * Creates a new {@link GroundData} without an impact block.
     *
     * @param groundBlock      the ground block
     * @param additionalBlocks the additional blocks
     */
    public GroundData(Block groundBlock, @Nullable List<Block> additionalBlocks) {
        this(groundBlock, additionalBlocks, null);
    }

    /**
     * Checks if the ground data has additional blocks.
     *
     * @return {@code true} if the ground data has additional blocks
     */
    public boolean hasAdditionalBlocks() {
        return additionalBlocks != null && !additionalBlocks.isEmpty();
    }

    /**
     * Checks if the ground data has an impact block.
     *
     * @return {@code true} if the ground data has an impact block
     */
    public boolean hasImpactBlock() {
        return impactBlock != null;
    }

    /**
     * Returns a random additional block from the list.
     *
     * @return the additional block
     */
    public Block getAddtionalBlock() {
        if (additionalBlocks == null || additionalBlocks.isEmpty()) return groundBlock();
        if (additionalBlocks.size() == 1) return additionalBlocks.getFirst();
        int index = ThreadLocalRandom.current().nextInt(0, this.additionalBlocks.size());
        return additionalBlocks.get(index);
    }
}
