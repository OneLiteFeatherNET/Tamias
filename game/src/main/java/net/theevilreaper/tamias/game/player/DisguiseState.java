package net.theevilreaper.tamias.game.player;

/**
 * Describes what other players see in place of a {@link TamiasPlayer}.
 * The server-side entity always stays a player; only the packets sent to viewers change.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum DisguiseState {

    /**
     * Viewers see the normal player.
     */
    NONE,
    /**
     * Viewers see a placed TNT block (a falling block entity without gravity).
     */
    TNT_BLOCK,
    /**
     * Viewers see a blinking primed TNT.
     */
    PRIMED_TNT,
    /**
     * Viewers see nothing.
     */
    HIDDEN
}
