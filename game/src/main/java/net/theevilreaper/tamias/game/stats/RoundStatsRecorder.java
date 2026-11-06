package net.theevilreaper.tamias.game.stats;

import net.minestom.server.entity.Player;
import net.onelitefeather.tamias.stats.StatsRepository;
import net.onelitefeather.tamias.stats.round.FinishedRound;
import net.onelitefeather.tamias.stats.round.StatsRole;
import net.theevilreaper.tamias.game.player.RoundRole;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.xerus.api.team.Team;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * The {@link RoundStatsRecorder} turns every regularly finished round into a {@link FinishedRound} and hands it to the
 * {@link StatsRepository}. Aborted rounds are not recorded.
 * <p>
 * A player is part of the round when they are in the survivor or the bomber team at round end and had a start role.
 * The win follows the same rule as {@link net.theevilreaper.tamias.game.round.RoundWins}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RoundStatsRecorder {

    private final StatsRepository repository;
    private final UUID gameId;
    private final Supplier<String> mapName;
    private final IntSupplier roundNumber;
    private final InstantSource clock;
    private @Nullable Instant startedAt;

    /**
     * Creates a new {@link RoundStatsRecorder} for one game.
     *
     * @param repository  the repository which stores the rounds
     * @param mapName     provides the name of the map the round is played on
     * @param roundNumber provides the number of the running round, starting at {@code 1}
     * @param clock       the source of the round times
     */
    public RoundStatsRecorder(StatsRepository repository, Supplier<String> mapName, IntSupplier roundNumber,
                              InstantSource clock) {
        this.repository = repository;
        this.gameId = UUID.randomUUID();
        this.mapName = mapName;
        this.roundNumber = roundNumber;
        this.clock = clock;
    }

    /**
     * Remembers when the round started. Must be called when the playing phase begins.
     */
    public void roundStarted() {
        this.startedAt = this.clock.instant();
    }

    /**
     * Records the finished round. Must be called before the teams are cleared.
     *
     * @param survivorTeam the survivor team
     * @param bomberTeam   the bomber team
     */
    public void roundEnded(Team survivorTeam, Team bomberTeam) {
        Instant start = this.startedAt;
        this.startedAt = null;
        if (start == null || !this.repository.enabled()) return;

        FinishedRound round = collect(survivorTeam, bomberTeam, start);
        if (round.players().isEmpty()) return;
        this.repository.saveRound(round);
    }

    private FinishedRound collect(Team survivorTeam, Team bomberTeam, Instant start) {
        boolean survivorsWon = RoundConditions.getWinner(survivorTeam, bomberTeam) == survivorTeam;
        StatsRole winner = survivorsWon ? StatsRole.SURVIVOR : StatsRole.BOMBER;

        List<FinishedRound.PlayerResult> players = new ArrayList<>();
        addResults(players, survivorTeam, false, winner);
        addResults(players, bomberTeam, true, winner);
        return new FinishedRound(UUID.randomUUID(), this.gameId, this.roundNumber.getAsInt(), this.mapName.get(),
                winner, start, this.clock.instant(), players);
    }

    private static void addResults(List<FinishedRound.PlayerResult> results, Team team, boolean bomberTeam,
                                   StatsRole winner) {
        for (Player player : team.getPlayers()) {
            if (!(player instanceof TamiasPlayer tamiasPlayer)) continue;
            StatsRole startRole = toStatsRole(tamiasPlayer.getStartRole());
            if (startRole == null) continue;

            StatsRole endRole = bomberTeam ? StatsRole.BOMBER : StatsRole.SURVIVOR;
            results.add(new FinishedRound.PlayerResult(
                    tamiasPlayer.getUuid(),
                    tamiasPlayer.getUsername(),
                    startRole,
                    startRole == winner && endRole == winner,
                    startRole == StatsRole.SURVIVOR && bomberTeam,
                    tamiasPlayer.getRoundKills(),
                    tamiasPlayer.getRoundHits()
            ));
        }
    }

    private static @Nullable StatsRole toStatsRole(RoundRole role) {
        return switch (role) {
            case SURVIVOR -> StatsRole.SURVIVOR;
            case BOMBER -> StatsRole.BOMBER;
            case NONE -> null;
        };
    }
}
