package net.onelitefeather.tamias.stats.round;

/**
 * The role a player had when a round started. Kept apart from the game's own role enum so the stats module does not
 * depend on the game module.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
public enum StatsRole {
    SURVIVOR,
    BOMBER
}
