package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

/**
 * A helper class which stored some constants which are required to build the game area into a {@link Instance}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GameAreaHelper {

    /**
     * The block that marks a position within the defined area as ground that should be replaced during placement.
     * Only positions holding this block are kept once the area's chunks are scanned.
     */
    public static final Block GROUND_MARKER_BLOCK = Block.BLUE_ICE;

    public static final int BLOCKS_PER_STEP = 20;
    public static final int TNT_SPAWN_HEIGHT = 20;
    public static final int MIN_TNT_AMOUNT = 15;
    public static final int MAX_TNT_AMOUNT = 35;
    public static final int MIN_SPEED_BOOST_AMOUNT = 10;
    public static final int MAX_SPEED_BOOST_AMOUNT = 20;

    private GameAreaHelper() {
        throw new UnsupportedOperationException("This class can't be instantiated");
    }
}
