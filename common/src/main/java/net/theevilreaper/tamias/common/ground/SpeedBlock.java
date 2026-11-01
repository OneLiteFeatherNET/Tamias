package net.theevilreaper.tamias.common.ground;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.util.Tags;

/**
 * A speed block is a normal ground block with the {@link Tags#SPEED_BOOST} tag.
 * Players which step onto it get a short velocity boost, the value of the tag is the strength of the boost.
 * The tag is used instead of the block type, so an accent block of the same material stays a normal block.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SpeedBlock {

    /**
     * The default horizontal boost in blocks per second.
     */
    public static final double DEFAULT_BOOST = 14.0;

    /**
     * Turns the given block into a speed block with the {@link #DEFAULT_BOOST}.
     *
     * @param block the block to mark
     * @return the block with the speed tag
     */
    public static Block of(Block block) {
        return of(block, DEFAULT_BOOST);
    }

    /**
     * Turns the given block into a speed block with the given boost.
     *
     * @param block the block to mark
     * @param boost the horizontal boost in blocks per second
     * @return the block with the speed tag
     */
    public static Block of(Block block, double boost) {
        Check.argCondition(block.air(), "A speed block can't be air");
        Check.argCondition(boost <= 0, "The boost must be positive");
        return block.withTag(Tags.SPEED_BOOST, boost);
    }

    /**
     * Checks if the given block is a speed block.
     *
     * @param block the block to check
     * @return {@code true} if the block has the speed tag
     */
    public static boolean isSpeedBlock(Block block) {
        return block.hasTag(Tags.SPEED_BOOST);
    }

    /**
     * Returns the boost of the given block.
     *
     * @param block the block to read
     * @return the horizontal boost in blocks per second, {@code 0} for a normal block
     */
    public static double getBoost(Block block) {
        Double boost = block.getTag(Tags.SPEED_BOOST);
        return boost == null ? 0 : boost;
    }

    private SpeedBlock() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
