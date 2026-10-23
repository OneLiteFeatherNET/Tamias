package net.theevilreaper.tamias.game.attribute;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;

/**
 * The {@link AttributeHelper} is a helper class to adjust some attributes of the player.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class AttributeHelper {

    private static final Key FREEZE_KEY = Key.key("tamias", "freeze");
    // Multiplies the total value by zero, so the base value of the player stays untouched
    private static final AttributeModifier FREEZE_MODIFIER =
            new AttributeModifier(FREEZE_KEY, -1.0, AttributeOperation.ADD_MULTIPLIED_TOTAL);
    // The client scales the FOV by the movement speed, unless the walking speed of the abilities is zero
    private static final float FROZEN_FIELD_VIEW_MODIFIER = 0.0f;
    private static final float DEFAULT_FIELD_VIEW_MODIFIER = 0.1f;

    /**
     * Disables the movement of the player.
     *
     * @param player the player to disable the movement
     */
    public static void disableMovement(Player player) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(FREEZE_MODIFIER);
        player.getAttribute(Attribute.JUMP_STRENGTH).addModifier(FREEZE_MODIFIER);
        player.setFieldViewModifier(FROZEN_FIELD_VIEW_MODIFIER);
    }

    /**
     * Enables the movement for the player.
     *
     * @param player the player to enable the movement
     */
    public static void enableMovement(Player player) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(FREEZE_KEY);
        player.getAttribute(Attribute.JUMP_STRENGTH).removeModifier(FREEZE_KEY);
        player.setFieldViewModifier(DEFAULT_FIELD_VIEW_MODIFIER);
    }

    private AttributeHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
