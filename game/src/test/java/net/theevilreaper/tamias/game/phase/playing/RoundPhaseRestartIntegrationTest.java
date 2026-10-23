package net.theevilreaper.tamias.game.phase.playing;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The phases of a round are restarted by the {@link net.theevilreaper.xerus.api.phase.CyclicPhaseSeries} for each
 * round. Xerus does not reset the ticks of a {@link TimedPhase}, so each phase has to restore its duration on start.
 */
class RoundPhaseRestartIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testPrePlayingPhaseRestoresTicksOnRestart(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);

        PrePlayingPhase phase = new PrePlayingPhase(teamService);
        int initialTicks = phase.getCurrentTicks();

        runAndRestart(phase);

        assertEquals(initialTicks, phase.getCurrentTicks());
        env.destroyInstance(instance, true);
    }

    @Test
    void testPrePlayingPhaseAllocatesTeamsAfterTheCountdown(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        env.createPlayer(instance);
        env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);

        PrePlayingPhase phase = new PrePlayingPhase(teamService);
        phase.start();
        phase.setCurrentTicks(0);
        phase.finish();

        assertEquals(2, countTeamPlayers(teamService));
        env.destroyInstance(instance, true);
    }

    @Test
    void testPrePlayingPhaseSkipsTheTeamAllocationWhenFinishedEarly(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);

        PrePlayingPhase phase = new PrePlayingPhase(teamService);
        phase.start();
        phase.finish();

        assertEquals(0, countTeamPlayers(teamService));
        assertFalse(player.hasTag(Tags.TEAM_KEY));
        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());
        env.destroyInstance(instance, true);
    }

    @Test
    void testPrePlayingPhaseFinishedEarlyWithoutPlayersDoesNotThrow() {
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);

        PrePlayingPhase phase = new PrePlayingPhase(teamService);
        phase.start();

        assertDoesNotThrow(phase::finish);
        assertTrue(phase.isFinished());
    }

    @Test
    void testPlayingPhaseRestoresTicksOnRestart() {
        PlayingPhase phase = new PlayingPhase(_ -> {}, () -> {}, HashMap::new);
        int initialTicks = phase.getCurrentTicks();

        runAndRestart(phase);

        assertEquals(initialTicks, phase.getCurrentTicks());
    }

    @Test
    void testPlayingPhaseShowsFullTimeBeforeStartLogic(@NotNull Env env) {
        List<String> calls = new ArrayList<>();
        PlayingPhase phase = new PlayingPhase(time -> calls.add("time " + time), () -> calls.add("start"), HashMap::new);

        phase.start();

        assertEquals(List.of("time 300", "start"), calls);
    }

    @Test
    void testPostPlayingPhaseRestoresTicksOnRestart() {
        PostPlayingPhase phase = new PostPlayingPhase();
        int initialTicks = phase.getCurrentTicks();

        runAndRestart(phase);

        assertEquals(initialTicks, phase.getCurrentTicks());
    }

    private int countTeamPlayers(@NotNull TeamService teamService) {
        return teamService.getTeams().stream().mapToInt(team -> team.getPlayers().size()).sum();
    }

    /**
     * Simulates what the cyclic series does with a phase between two rounds: the phase runs down to zero,
     * finishes and is started again.
     *
     * @param phase the phase to cycle
     */
    private void runAndRestart(@NotNull TimedPhase phase) {
        phase.start();
        phase.setCurrentTicks(0);
        phase.finish();
        phase.setFinished(false);
        phase.start();
    }
}
