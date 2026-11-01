package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.random.RandomGenerator;

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
     * The amount of TNT blocks a round gets regardless of the players.
     */
    public static final int TNT_BASE_AMOUNT = 10;
    /**
     * The amount of TNT blocks which is added for each player.
     */
    public static final int TNT_PER_PLAYER = 6;
    /**
     * The TNT amount of a round varies randomly by up to this many blocks in both directions.
     */
    public static final int TNT_AMOUNT_VARIANCE = 4;
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

    /**
     * Calculates the amount of TNT blocks for a round, it grows with the amount of players.
     * With 13 players a round gets about 88 TNT blocks.
     *
     * @param players the amount of players in the game
     * @param random  the random generator for the variance
     * @return the amount of TNT blocks to drop
     */
    public static int tntAmount(int players, RandomGenerator random) {
        int amount = TNT_BASE_AMOUNT + TNT_PER_PLAYER * Math.max(0, players);
        return amount + random.nextInt(-TNT_AMOUNT_VARIANCE, TNT_AMOUNT_VARIANCE + 1);
    }

    private GameAreaHelper() {
        throw new UnsupportedOperationException("This class can't be instantiated");
    }
}
