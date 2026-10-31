package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.util.Tags;

import java.time.Duration;

/**
 * The {@link SpawnProtection} protects a bomber for a short time after the spawn on a TNT block.
 * Eggs have no effect on a protected bomber, so a survivor can't shoot the bomber directly after the respawn.
 * The protection ends on its own after {@link #DURATION} or when the bomber primes the bomb.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SpawnProtection {

    public static final Duration DURATION = Duration.ofSeconds(3);

    /**
     * Protects the player for the {@link #DURATION}.
     *
     * @param player the player to protect
     */
    public static void enable(Player player) {
        player.setTag(Tags.SPAWN_PROTECTION, System.currentTimeMillis() + DURATION.toMillis());
    }

    /**
     * Removes the protection of the player.
     *
     * @param player the player to remove the protection from
     */
    public static void disable(Player player) {
        player.removeTag(Tags.SPAWN_PROTECTION);
    }

    /**
     * Returns whether the player is still protected.
     *
     * @param player the player to check
     * @return true if the protection of the player is not over
     */
    public static boolean isProtected(Player player) {
        Long protectedUntil = player.getTag(Tags.SPAWN_PROTECTION);
        return protectedUntil != null && System.currentTimeMillis() < protectedUntil;
    }

    private SpawnProtection() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
