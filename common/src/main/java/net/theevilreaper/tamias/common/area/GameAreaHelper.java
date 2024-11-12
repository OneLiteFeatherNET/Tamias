package net.theevilreaper.tamias.common.area;

import net.minestom.server.instance.Instance;

/**
 * A helper class which stored some constants which are required to build the game area into a {@link Instance}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GameAreaHelper {

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
