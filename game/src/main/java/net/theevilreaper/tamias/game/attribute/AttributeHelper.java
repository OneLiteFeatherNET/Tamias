package net.theevilreaper.tamias.game.attribute;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("java:S3252")
public class AttributeHelper {

    private static final double ZERO_MOVEMENT = 0.0;
    private static final double DEFAULT_MOVE_SPEED = 0.1;

    /**
     * Disables the movement for the player.
     * @param player the player to disable the movement
     */
    public static void disableMovement(@NotNull Player player) {
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(ZERO_MOVEMENT);
    }

    /**
     * Enables the movement for the player.
     * @param player the player to enable the movement
     */
    public static void enableMovement(@NotNull Player player) {
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(DEFAULT_MOVE_SPEED);
    }

    private AttributeHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
