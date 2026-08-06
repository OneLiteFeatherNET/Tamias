package net.theevilreaper.tamias.game.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TeleportationLogicTest {

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
    }

    @AfterEach
    void tearDown() {
        for (Team team : teamService.getTeams()) {
            team.getPlayers().clear();
        }
    }

    @Test
    void testTeleportTeamToPosition(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, player);

        Pos targetPos = new Pos(10, 64, 10);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        TeleportationLogic.teleport(survivorTeam, targetPos, p -> callbackExecuted.set(true)).join();

        assertEquals(targetPos, player.getPosition());
        assertTrue(callbackExecuted.get());

        env.destroyInstance(instance, true);
    }
}
