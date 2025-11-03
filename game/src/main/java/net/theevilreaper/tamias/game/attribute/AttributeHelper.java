package net.theevilreaper.tamias.game.attribute;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;

/**
 * The {@link AttributeHelper} is a helper class to adjust some attributes of the player.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class AttributeHelper {

    private static final double ZERO_MOVEMENT = 0.0;
    private static final double DEFAULT_MOVE_SPEED = 0.1;

    private static final double DEFAULT_JUMP_HEIGHT = 0.42;
    private static final double ZERO_JUMP = 0.0;

    /**
     * Disables the movement of the player.
     *
     * @param player the player to disable the movement
     */
    public static void disableMovement(Player player) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(ZERO_MOVEMENT);
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(ZERO_JUMP);
    }

    /**
     * Enables the movement for the player.
     *
     * @param player the player to enable the movement
     */
    public static void enableMovement(Player player) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(DEFAULT_MOVE_SPEED);
        player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(DEFAULT_JUMP_HEIGHT);
    }

    private AttributeHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
