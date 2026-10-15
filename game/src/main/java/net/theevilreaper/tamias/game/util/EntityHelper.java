package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;

/**
 * The {@link EntityHelper} provides some utility methods to handle entities.
 * This class is not intended to be instantiated.
 * It changes how a player is shown to other players, see {@link DisguiseState}.
 * The server-side entity type of the player is never changed.
 *
 * @author theEvilReaper
 * @version 2.0.0
 * @since 1.0.0
 */
public final class EntityHelper {

    /**
     * Shows the player as a placed TNT block to other players.
     *
     * @param player the player to switch
     */
    public static void switchToTNT(Player player) {
        disguise(player, DisguiseState.TNT_BLOCK);
    }

    /**
     * Shows the player as a blinking primed TNT to other players.
     *
     * @param player the player to prime
     */
    public static void prime(Player player) {
        disguise(player, DisguiseState.PRIMED_TNT);
    }

    /**
     * Hides the player from other players.
     *
     * @param player the player to hide
     */
    public static void hide(Player player) {
        disguise(player, DisguiseState.HIDDEN);
    }

    /**
     * Shows the player as a normal player to other players again.
     *
     * @param player the player to switch
     */
    public static void switchToPlayer(Player player) {
        disguise(player, DisguiseState.NONE);
    }

    /**
     * Every online player is a {@link TamiasPlayer}, see {@code TamiasLauncher}.
     */
    private static void disguise(Player player, DisguiseState state) {
        ((TamiasPlayer) player).setDisguise(state);
    }

    private EntityHelper() {

    }
}
