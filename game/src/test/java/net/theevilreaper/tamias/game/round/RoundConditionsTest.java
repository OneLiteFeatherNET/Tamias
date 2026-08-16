package net.theevilreaper.tamias.game.round;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class RoundConditionsTest {

    private TeamService teamService;
    private BomberTicketService ticketService;

    @BeforeEach
    void setUp() {
        teamService = TeamService.of();
        TeamHelper.loadTeams(4, teamService);
        ticketService = new BomberTicketService();
    }

    @AfterEach
    void tearDown() {
        for (Team team : teamService.getTeams()) {
            team.clearPlayers();
        }
    }

    private PlayingPhase startPlayingPhase(LinearPhaseSeries<Phase> phaseSeries) {
        PlayingPhase playingPhase = new PlayingPhase(ticks -> {}, () -> {}, Map::of);
        phaseSeries.add(playingPhase);
        phaseSeries.start();
        return playingPhase;
    }

    @Test
    void testRoundEndsWhenTicketsExhaustedEvenIfTeamsAreNotEmpty(@NotNull Env env) {
        LinearPhaseSeries<Phase> phaseSeries = new LinearPhaseSeries<>("Test");
        PlayingPhase playingPhase = startPlayingPhase(phaseSeries);

        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance, Pos.ZERO);
        Player bomber = env.createPlayer(instance, Pos.ZERO);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), survivor);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);

        ticketService.start(1, 0);

        RoundConditions.checkRoundEnd(phaseSeries, teamService, ticketService);

        assertTrue(playingPhase.isFinished());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundContinuesWhenTicketsAndTeamsRemain(@NotNull Env env) {
        LinearPhaseSeries<Phase> phaseSeries = new LinearPhaseSeries<>("Test");
        PlayingPhase playingPhase = startPlayingPhase(phaseSeries);

        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance, Pos.ZERO);
        Player bomber = env.createPlayer(instance, Pos.ZERO);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), survivor);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);

        ticketService.start(1, 5);

        RoundConditions.checkRoundEnd(phaseSeries, teamService, ticketService);

        assertFalse(playingPhase.isFinished());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndsWhenSurvivorTeamEmpty(@NotNull Env env) {
        LinearPhaseSeries<Phase> phaseSeries = new LinearPhaseSeries<>("Test");
        PlayingPhase playingPhase = startPlayingPhase(phaseSeries);

        Instance instance = env.createFlatInstance();
        Player bomber = env.createPlayer(instance, Pos.ZERO);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);

        ticketService.start(1, 5);

        RoundConditions.checkRoundEnd(phaseSeries, teamService, ticketService);

        assertTrue(playingPhase.isFinished());

        env.destroyInstance(instance, true);
    }
}
