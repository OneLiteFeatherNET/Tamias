package net.onelitefeather.tamias.stats.round;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A finished round as the game hands it over to the {@link net.onelitefeather.tamias.stats.StatsRepository}.
 *
 * @param roundId     the id of the round
 * @param gameId      the id of the game the round belongs to
 * @param roundNumber the number of the round inside its game, starting at 1
 * @param mapName     the map the round was played on
 * @param winner      the side which won the round
 * @param startedAt   when the round started
 * @param finishedAt  when the round ended
 * @param players     the result of every participant
 * @version 1.0.0
 * @since 0.1.0
 */
public record FinishedRound(
        UUID roundId,
        UUID gameId,
        int roundNumber,
        String mapName,
        StatsRole winner,
        Instant startedAt,
        Instant finishedAt,
        List<PlayerResult> players
) {

    public FinishedRound {
        players = List.copyOf(players);
    }

    /**
     * The result of one player in a round.
     *
     * @param playerUuid the player
     * @param playerName the name the player had in the round
     * @param startRole  the role at round start
     * @param won        whether the round win was credited to the player
     * @param converted  whether the player started as survivor and was turned into a bomber
     * @param kills      kills as bomber in this round
     * @param hits       hits as survivor in this round
     * @param explosions    explosions as bomber in this round, including the ones where the bomber was shot
     */
    public record PlayerResult(
            UUID playerUuid,
            String playerName,
            StatsRole startRole,
            boolean won,
            boolean converted,
            int kills,
            int hits,
            int explosions
    ) {
    }
}
