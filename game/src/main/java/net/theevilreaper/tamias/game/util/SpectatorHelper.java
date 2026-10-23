package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.TimedPotion;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link SpectatorHelper} handles the light spectator mode.
 * A bomber becomes a spectator when no TNT is left to respawn on. The player stays in the bomber team,
 * can fly around invisible and is not able to interact with the game until the round ends.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SpectatorHelper {

    /**
     * Turns the player into a spectator.
     *
     * @param player the player to turn into a spectator
     */
    public static void enable(Player player) {
        player.setTag(Tags.SPECTATOR, true);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();

        List<TimedPotion> activeEffects = new ArrayList<>(player.getActiveEffects());
        for (TimedPotion activeEffect : activeEffects) {
            player.removeEffect(activeEffect.potion().effect());
        }

        // An exploded bomber can't move, the spectator must be able to fly around
        AttributeHelper.enableMovement(player);
        player.setAllowFlying(true);
        player.setFlying(true);
        EntityHelper.hide(player);
    }

    /**
     * Resets the spectator state of the player.
     * The visibility is restored by {@link EntityHelper#switchToPlayer(Player)}.
     *
     * @param player the player to reset
     */
    public static void disable(Player player) {
        player.removeTag(Tags.SPECTATOR);
        player.setFlying(false);
        player.setAllowFlying(false);
        player.setGameMode(GameMode.ADVENTURE);
    }

    /**
     * Returns whether the player is a spectator.
     *
     * @param player the player to check
     * @return true if the player is a spectator
     */
    public static boolean isSpectator(Player player) {
        return player.hasTag(Tags.SPECTATOR);
    }

    private SpectatorHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
