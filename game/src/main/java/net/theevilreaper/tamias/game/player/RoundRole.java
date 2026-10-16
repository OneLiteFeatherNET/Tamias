package net.theevilreaper.tamias.game.player;

/**
 * Describes the role a {@link TamiasPlayer} had when the current round started.
 * Survivors can become bombers during a round, so the start role decides who gets the round win.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public enum RoundRole {

    /**
     * The player has not taken part in a round yet.
     */
    NONE,
    /**
     * The player started the round as a survivor.
     */
    SURVIVOR,
    /**
     * The player started the round as a bomber.
     */
    BOMBER
}
