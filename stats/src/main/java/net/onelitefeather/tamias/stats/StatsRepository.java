package net.onelitefeather.tamias.stats;

import net.onelitefeather.tamias.stats.player.PlayerStatsView;
import net.onelitefeather.tamias.stats.player.TamiasStatsPlayer;
import net.onelitefeather.tamias.stats.round.FinishedRound;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StatsRepository extends AutoCloseable {

    /**
     * Tells whether statistics are stored at all.
     *
     * @return {@code false} for the repository that keeps nothing
     */
    default boolean enabled() {
        return true;
    }

    /**
     * Stores a finished round. Implementations do the I/O off the calling thread and never throw: a
     * failed write is logged and counted, it must not touch the round end.
     *
     * @param round the finished round
     * @return completes after the commit, or exceptionally when the write failed
     */
    CompletableFuture<Void> saveRound(FinishedRound round);

    /**
     * Finds a player by the last name they were seen with, ignoring case. When several players have
     * carried the name, the one whose name was seen last wins.
     *
     * @param name the name to look for
     * @return the player, empty when no player was seen with that name
     */
    CompletableFuture<Optional<TamiasStatsPlayer>> findPlayerByName(String name);

    /**
     * Sums up the statistics of a player over every stored round.
     *
     * @param playerUuid the player
     * @return the statistics, empty when the player has no rounds
     */
    CompletableFuture<Optional<PlayerStatsView>> findTotals(UUID playerUuid);

    /**
     * Deletes every round record and the stored name of a player in one transaction.
     * Blocks until done, so call it off the tick thread.
     *
     * @param playerUuid the player
     * @return how many rows were removed in total
     */
    int deleteByPlayer(UUID playerUuid);

    /**
     * Writes what is still queued and releases the connections.
     */
    @Override
    void close();
}
