package net.onelitefeather.tamias.stats;

import net.onelitefeather.tamias.stats.player.TamiasStatsPlayer;
import net.onelitefeather.tamias.stats.round.FinishedRound;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The repository used while the statistics are off. It keeps nothing and finds nothing.
 *
 * @version 1.0.0
 * @since 0.1.0
 */
final class DisabledStatsRepository implements StatsRepository {

    static final DisabledStatsRepository INSTANCE = new DisabledStatsRepository();

    private DisabledStatsRepository() {
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public CompletableFuture<Void> saveRound(FinishedRound round) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<TamiasStatsPlayer>> findPlayerByName(String name) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public int deleteByPlayer(UUID playerUuid) {
        return 0;
    }

    @Override
    public void close() {
        // nothing to release
    }
}
