package net.theevilreaper.tamias.game.stamina;

import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class StaminaServiceIntegrationTest {

    private StaminaService staminaService;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        staminaService = new StaminaService();
        teamService = TeamService.of();
        TeamHelper.loadTeams(1, teamService);
    }

    @AfterEach
    void tearDown() {
        staminaService.cleanUp();
        for (int i = 0; i < teamService.getTeams().size(); i++) {
            Team team = teamService.getTeams().get(i);
            team.clearPlayers();
        }
    }



    @Test
    void testCreateStaminaObjects(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        Player secondPlayer = env.createPlayer(instance);

        teamService.getTeam(GameConfig.SURVIVOR_KEY).ifPresent(t -> TeamHelper.addPlayerToTeam(t, player));
        teamService.getTeam(GameConfig.BOMBER_KEY).ifPresent(t -> TeamHelper.addPlayerToTeam(t, secondPlayer));

        staminaService.createStaminaObjects(teamService);

        StaminaBar staminaBar = staminaService.getStaminaBar(player.getUuid());
        assertNotNull(staminaBar);
        assertInstanceOf(ShootBar.class, staminaBar);

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

        teamService.getTeam(GameConfig.SURVIVOR_KEY).ifPresent(t -> TeamHelper.addPlayerToTeam(t, player));

        staminaService.createStaminaObjects(teamService);

        assertTrue(staminaService.removeStaminaBar(player.getUuid()));
        assertFalse(staminaService.removeStaminaBar(secondPlayer.getUuid()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testStaminaGet(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance);

        teamService.getTeam(GameConfig.SURVIVOR_KEY).ifPresent(t -> TeamHelper.addPlayerToTeam(t, firstPlayer));

        staminaService.createStaminaObjects(teamService);

        Player secondPlayer = env.createPlayer(instance);

        assertNotNull(staminaService.getStaminaBar(firstPlayer));
        assertNull(staminaService.getStaminaBar(secondPlayer));

        env.destroyInstance(instance, true);
    }

    @Test
    void testStartAndCleanUpFlow(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        teamService.getTeam(GameConfig.SURVIVOR_KEY).ifPresent(t -> TeamHelper.addPlayerToTeam(t, player));
        staminaService.createStaminaObjects(teamService);

        StaminaBar bar = staminaService.getStaminaBar(player);
        assertNotNull(bar);
        assertNull(bar.status);

        staminaService.start();
        assertEquals(StaminaBar.Status.READY, bar.status);

        staminaService.cleanUp();
        assertNull(staminaService.getStaminaBar(player));
        assertNull(bar.status);

        env.destroyInstance(instance, true);
    }

    @Test
    void testAddManualStaminaBar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        StaminaBar shootBar = StaminaFactory.createShootBar(player);

        staminaService.add(player.getUuid(), shootBar);
        assertEquals(shootBar, staminaService.getStaminaBar(player.getUuid()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testTeamWithoutStaminaComponent(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        Team customTeam = Team.of(net.kyori.adventure.key.Key.key("tamias", "spec_team"), 10);
        customTeam.addPlayer(player);
        teamService.add(customTeam);

        staminaService.createStaminaObjects(teamService);
        assertNull(staminaService.getStaminaBar(player));

        env.destroyInstance(instance, true);
    }
}
