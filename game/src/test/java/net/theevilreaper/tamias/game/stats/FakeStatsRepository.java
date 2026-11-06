package net.theevilreaper.tamias.game.stats;

import net.onelitefeather.tamias.stats.StatsRepository;
import net.onelitefeather.tamias.stats.player.PlayerStatsView;
import net.onelitefeather.tamias.stats.player.TamiasStatsPlayer;
import net.onelitefeather.tamias.stats.round.FinishedRound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link StatsRepository} for tests which keeps everything in memory and answers at once.
 */
public final class FakeStatsRepository implements StatsRepository {

    private final boolean enabled;
    private final List<FinishedRound> rounds = new ArrayList<>();
    private final Map<UUID, PlayerStatsView> totals = new HashMap<>();
    private boolean failing;

    public FakeStatsRepository(boolean enabled) {
        this.enabled = enabled;
    }

    public List<FinishedRound> rounds() {
        return this.rounds;
    }

    public void addTotals(PlayerStatsView view) {
        this.totals.put(view.player().uuid(), view);
    }

    public void setFailing(boolean failing) {
        this.failing = failing;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public CompletableFuture<Void> saveRound(FinishedRound round) {
        this.rounds.add(round);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<TamiasStatsPlayer>> findPlayerByName(String name) {
        if (this.failing) return CompletableFuture.failedFuture(new IllegalStateException("database down"));
        String key = name.toLowerCase(Locale.ROOT);
        return CompletableFuture.completedFuture(this.totals.values().stream()
                .map(PlayerStatsView::player)
                .filter(player -> player.name().toLowerCase(Locale.ROOT).equals(key))
                .findFirst());
    }

    @Override
    public CompletableFuture<Optional<PlayerStatsView>> findTotals(UUID playerUuid) {
        if (this.failing) return CompletableFuture.failedFuture(new IllegalStateException("database down"));
        return CompletableFuture.completedFuture(Optional.ofNullable(this.totals.get(playerUuid)));
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
