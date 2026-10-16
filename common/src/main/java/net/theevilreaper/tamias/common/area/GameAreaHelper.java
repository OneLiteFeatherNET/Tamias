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
    /**
     * The amount of placement steps between two place sounds of the ground.
     */
    public static final int GROUND_SOUND_INTERVAL = 3;
    /**
     * The height above its target from which a TNT block falls. The whole fall path must be free.
     */
    public static final int TNT_DROP_HEIGHT = 5;
    public static final int MIN_TNT_AMOUNT = 15;
    public static final int MAX_TNT_AMOUNT = 35;
    /**
     * The share of the TNT blocks of a round which are dropped in groups instead of as single blocks.
     */
    public static final double TNT_GROUP_RATIO = 0.4;
    public static final int MIN_TNT_GROUP_SIZE = 2;
    public static final int MAX_TNT_GROUP_SIZE = 4;
    /**
     * The delay in ticks between the TNT blocks which pop up around a landed group center.
     */
    public static final int TNT_POP_DELAY = 2;
    /**
     * One special block is placed for each started amount of this many ground positions.
     */
    public static final int POSITIONS_PER_SPECIAL_BLOCK = 100;
    public static final int MIN_SPEED_BOOST_AMOUNT = 10;
    public static final int MAX_SPEED_BOOST_AMOUNT = 20;

    private GameAreaHelper() {
        throw new UnsupportedOperationException("This class can't be instantiated");
    }
}
