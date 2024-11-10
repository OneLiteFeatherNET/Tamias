package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import org.jetbrains.annotations.NotNull;

/**
 * The class contains some effects which are used during the game.
 * It has also some methods to add the effects to {@link Player}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public final class Effects {

    private static final TimedPotion BLINDNESS =
            new TimedPotion(new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE), Integer.MAX_VALUE);
    private static final TimedPotion SLOWNESS =
            new TimedPotion(new Potion(PotionEffect.SLOWNESS, (byte) 1, Integer.MAX_VALUE), Integer.MAX_VALUE);
    private static final TimedPotion NIGHT_VISION =
            new TimedPotion(new Potion(PotionEffect.NIGHT_VISION, (byte) 1, Integer.MAX_VALUE), 20L * 3);

    private Effects() {
    }

    /**
     * Set the required effects when a player wants to blow up someone.
     *
     * @param player the player who should receive the effects
     */
    public static void setEffectForExplode(@NotNull Player player) {
        clearEffects(player);
        player.addEffect(NIGHT_VISION.potion());
    }

    /**
     * Sets the required effects when a player got blown up or was shot by an egg.
     *
     * @param player the player who should receive the effects
     */
    public static void setExplodeOrHitEffect(@NotNull Player player) {
        clearEffects(player);
        player.addEffect(SLOWNESS.potion());
        player.addEffect(BLINDNESS.potion());
    }

    /**
     * Clears all effects from the player.
     *
     * @param player the player to clear the effects
     */
    private static void clearEffects(@NotNull Player player) {
        if (!player.getActiveEffects().isEmpty()) {
            for (TimedPotion activeEffect : player.getActiveEffects()) {
                player.removeEffect(activeEffect.potion().effect());
            }
        }
    }
}
