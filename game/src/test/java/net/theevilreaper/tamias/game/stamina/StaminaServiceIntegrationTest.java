package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.key.Key;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class StaminaServiceIntegrationTest {

    private static StaminaService staminaService;
    private static TeamService teamService;

    @BeforeAll
    static void setUp() {
        staminaService = new StaminaService();
        teamService = TeamService.of();
        Team testTeam = Team.of(Key.key("tamias", "shoot_team"), 1);
        Team testTeam2 = Team.of(Key.key("tamias", "explode_team"), 1);
        teamService.add(testTeam);
        teamService.add(testTeam2);
    }

    @AfterEach
    void tearDown() {
        staminaService.cleanUp();
        for (int i = 0; i < teamService.getTeams().size(); i++) {
            Team team = teamService.getTeams().get(i);
            team.clearPlayers();
        }
    }

    @AfterAll
    static void afterAll() {
        staminaService.cleanUp();
        staminaService = null;
    }

    @Test
    void testStaminaAdd(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        Player secondPlayer = env.createPlayer(instance);

        teamService.getTeams().getFirst().addPlayer(player);
        teamService.getTeams().getLast().addPlayer(secondPlayer);

        staminaService.createStaminaObjects(teamService);

        StaminaBar staminaBar = staminaService.getStaminaBar(player.getUuid());
        assertNotNull(staminaBar);
        assertInstanceOf(ShootBar.class, staminaBar);

        assertNotNull(staminaService.getStaminaBar(player.getUuid()));

        StaminaBar secondBar = staminaService.getStaminaBar(secondPlayer.getUuid());
        assertNotNull(secondBar);
        assertInstanceOf(ExplodeBar.class, secondBar);

        env.destroyInstance(instance, true);
    }

    @Test
    void testStaminaRemove(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        Player secondPlayer = env.createPlayer(instance);

        teamService.getTeams().getFirst().addPlayer(player);

        staminaService.createStaminaObjects(teamService);

        assertTrue(staminaService.removeStaminaBar(player.getUuid()));
        assertFalse(staminaService.removeStaminaBar(secondPlayer.getUuid()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testStaminaGet(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance);

        teamService.getTeams().getFirst().addPlayer(firstPlayer);

        staminaService.createStaminaObjects(teamService);

        Player secondPlayer = env.createPlayer(instance);

        assertNotNull(staminaService.getStaminaBar(firstPlayer));
        assertNull(staminaService.getStaminaBar(secondPlayer));

        env.destroyInstance(instance, true);
    }
}
