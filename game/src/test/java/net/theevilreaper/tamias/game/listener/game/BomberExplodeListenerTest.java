package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.bomber.BomberExplodeEvent;
import net.theevilreaper.tamias.game.round.BomberTicketService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class BomberExplodeListenerTest {

    private static final double RADIUS = 5.0;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = TeamService.of();
        TeamHelper.loadTeams(8, teamService);
    }

    @AfterEach
    void tearDown() {
        for (Team team : teamService.getTeams()) {
            team.clearPlayers();
        }
    }

    @Test
    void testConvertsSurvivorWithinRadius(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());

        Player bomber = env.createPlayer(instance, Pos.ZERO);
        Player survivor = env.createPlayer(instance, new Pos(2, 0, 0));

        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 10);
        AtomicInteger roundEndChecks = new AtomicInteger(0);

        BomberExplodeListener listener = new BomberExplodeListener(teamService, ticketService, RADIUS, roundEndChecks::incrementAndGet);
        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertFalse(survivorTeam.getPlayers().contains(survivor));
        assertTrue(bomberTeam.getPlayers().contains(survivor));
        assertEquals(GameConfig.BOMBER_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));
        assertEquals(9, ticketService.getRemaining());
        assertEquals(1, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testDoesNotConvertSurvivorOutsideRadius(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());

        Player bomber = env.createPlayer(instance, Pos.ZERO);
        Player survivor = env.createPlayer(instance, new Pos(20, 0, 0));

        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 10);

        BomberExplodeListener listener = new BomberExplodeListener(teamService, ticketService, RADIUS, () -> {});
        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertTrue(survivorTeam.getPlayers().contains(survivor));
        assertFalse(bomberTeam.getPlayers().contains(survivor));
        assertEquals(10, ticketService.getRemaining());

        env.destroyInstance(instance, true);
    }

    @Test
    void testConversionStopsWhenTicketsExhausted(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());

        Player bomber = env.createPlayer(instance, Pos.ZERO);
        Player firstSurvivor = env.createPlayer(instance, new Pos(1, 0, 0));
        Player secondSurvivor = env.createPlayer(instance, new Pos(2, 0, 0));

        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);
        TeamHelper.addPlayerToTeam(survivorTeam, firstSurvivor);
        TeamHelper.addPlayerToTeam(survivorTeam, secondSurvivor);

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 1);

        BomberExplodeListener listener = new BomberExplodeListener(teamService, ticketService, RADIUS, () -> {});
        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        long convertedCount = bomberTeam.getPlayers().stream()
                .filter(p -> p == firstSurvivor || p == secondSurvivor)
                .count();
        assertEquals(1, convertedCount);
        assertTrue(ticketService.isEmpty());

        env.destroyInstance(instance, true);
    }
}
