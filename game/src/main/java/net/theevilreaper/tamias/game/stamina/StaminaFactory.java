package net.theevilreaper.tamias.game.stamina;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * The factory class is responsible for creating new instances of the {@link StaminaBar}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@ApiStatus.Internal
public abstract class StaminaFactory {

    /**
     * Creates a new instance of the {@link StaminaBar} for the given player.
     *
     * @param player the player which should receive the stamina bar
     * @return a new instance of the {@link StaminaBar}
     */
    public static StaminaBar createExplodeBar(Player player) {
        return new ExplodeBar(player);
    }

    /**
     * Creates a new instance of the {@link StaminaBar} for the given player.
     *
     * @param player the player which should receive the stamina bar
     * @return a new instance of the {@link StaminaBar}
     */
    public static StaminaBar createShootBar(Player player) {
        return new ShootBar(player);
    }

    private StaminaFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
