package net.theevilreaper.tamias.game.stats;

import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.onelitefeather.tamias.stats.round.FinishedRound;
import net.onelitefeather.tamias.stats.round.StatsRole;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.round.RoundWins;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.InstantSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundStatsRecorderIntegrationTest extends TamiasPlayerTestBase {

    private static final Instant START = Instant.parse("2026-11-06T18:00:00Z");

    @Test
    void testSurvivorWinIsRecorded(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer survivor = createPlayer(env, instance);
        TamiasPlayer converted = createPlayer(env, instance);
        TamiasPlayer bomber = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(survivorTeam, converted);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        FakeStatsRepository repository = new FakeStatsRepository(true);
        MutableClock clock = new MutableClock(START);
        RoundStatsRecorder recorder = new RoundStatsRecorder(repository, () -> "Forest", () -> 2, clock);

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        recorder.roundStarted();
        survivor.addHit();
        survivor.addHit();
        bomber.addKill();
        TeamHelper.switchToTNTTeam(teamService, converted);
        clock.now = START.plusSeconds(90);
        recorder.roundEnded(survivorTeam, bomberTeam);

        assertEquals(1, repository.rounds().size());
        FinishedRound round = repository.rounds().getFirst();
        assertEquals(StatsRole.SURVIVOR, round.winner());
        assertEquals(2, round.roundNumber());
        assertEquals("Forest", round.mapName());
        assertEquals(START, round.startedAt());
        assertEquals(START.plusSeconds(90), round.finishedAt());
        assertEquals(3, round.players().size());

        FinishedRound.PlayerResult survivorResult = result(round, survivor);
        assertEquals(StatsRole.SURVIVOR, survivorResult.startRole());
        assertTrue(survivorResult.won());
        assertFalse(survivorResult.converted());
        assertEquals(2, survivorResult.hits());
        assertEquals(survivor.getUsername(), survivorResult.playerName());

        FinishedRound.PlayerResult convertedResult = result(round, converted);
        assertEquals(StatsRole.SURVIVOR, convertedResult.startRole());
        assertFalse(convertedResult.won());
        assertTrue(convertedResult.converted());

        FinishedRound.PlayerResult bomberResult = result(round, bomber);
        assertEquals(StatsRole.BOMBER, bomberResult.startRole());
        assertFalse(bomberResult.won());
        assertEquals(1, bomberResult.kills());

        env.destroyInstance(instance, true);
    }

    @Test
    void testBomberWinCreditsOnlyStartBombers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer survivor = createPlayer(env, instance);
        TamiasPlayer bomber = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        FakeStatsRepository repository = new FakeStatsRepository(true);
        RoundStatsRecorder recorder = new RoundStatsRecorder(repository, () -> "Forest", () -> 1, new MutableClock(START));

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        recorder.roundStarted();
        TeamHelper.switchToTNTTeam(teamService, survivor);
        recorder.roundEnded(survivorTeam, bomberTeam);

        FinishedRound round = repository.rounds().getFirst();
        assertEquals(StatsRole.BOMBER, round.winner());
        assertTrue(result(round, bomber).won());
        assertFalse(result(round, survivor).won());
        assertTrue(result(round, survivor).converted());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundStatsAreResetForEachRound(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer survivor = createPlayer(env, instance);
        TamiasPlayer bomber = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        FakeStatsRepository repository = new FakeStatsRepository(true);
        RoundStatsRecorder recorder = new RoundStatsRecorder(repository, () -> "Forest", () -> 1, new MutableClock(START));

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        recorder.roundStarted();
        survivor.addHit();
        recorder.roundEnded(survivorTeam, bomberTeam);

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        recorder.roundStarted();
        recorder.roundEnded(survivorTeam, bomberTeam);

        assertEquals(1, result(repository.rounds().get(0), survivor).hits());
        assertEquals(0, result(repository.rounds().get(1), survivor).hits());
        assertEquals(1, survivor.getHits());
        assertEquals(repository.rounds().get(0).gameId(), repository.rounds().get(1).gameId());

        env.destroyInstance(instance, true);
    }

    @Test
    void testNothingIsRecordedWhenDisabledOrNotStarted(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, createPlayer(env, instance));
        TeamHelper.addPlayerToTeam(bomberTeam, createPlayer(env, instance));
        RoundWins.snapshotRoles(survivorTeam, bomberTeam);

        FakeStatsRepository disabled = new FakeStatsRepository(false);
        RoundStatsRecorder disabledRecorder = new RoundStatsRecorder(disabled, () -> "Forest", () -> 1, new MutableClock(START));
        disabledRecorder.roundStarted();
        disabledRecorder.roundEnded(survivorTeam, bomberTeam);
        assertTrue(disabled.rounds().isEmpty());

        FakeStatsRepository enabled = new FakeStatsRepository(true);
        RoundStatsRecorder notStarted = new RoundStatsRecorder(enabled, () -> "Forest", () -> 1, new MutableClock(START));
        notStarted.roundEnded(survivorTeam, bomberTeam);
        assertTrue(enabled.rounds().isEmpty());

        env.destroyInstance(instance, true);
    }

    private static FinishedRound.PlayerResult result(FinishedRound round, TamiasPlayer player) {
        return round.players().stream()
                .filter(result -> result.playerUuid().equals(player.getUuid()))
                .findFirst()
                .orElseThrow();
    }

    private @NotNull TeamService createTeams() {
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(4, teamService);
        return teamService;
    }

    private @NotNull TamiasPlayer createPlayer(@NotNull Env env, @NotNull Instance instance) {
        return (TamiasPlayer) env.createPlayer(instance);
    }

    private static final class MutableClock implements InstantSource {

        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant instant() {
            return this.now;
        }
    }
}
