package net.onelitefeather.tamias.stats.player;

/**
 * The statistics of a player summed up over every stored round.
 *
 * @param player       the player with the name they were seen with last
 * @param roundsPlayed how many rounds the player took part in
 * @param roundWins    how many of them the player won
 * @param survivorWins the wins of rounds the player started as survivor
 * @param bomberWins   the wins of rounds the player started as bomber
 * @param kills        survivors converted as bomber
 * @param hits         bombers hit as survivor
 * @version 1.0.0
 * @since 0.1.0
 */
public record PlayerStatsView(TamiasStatsPlayer player, long roundsPlayed, long roundWins, long survivorWins,
                              long bomberWins, long kills, long hits) {
}
