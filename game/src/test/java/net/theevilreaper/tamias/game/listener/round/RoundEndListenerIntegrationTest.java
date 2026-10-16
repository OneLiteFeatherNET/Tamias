package net.theevilreaper.tamias.game.listener.round;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundEndListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testRoundEndShowsBomberAsPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, player);
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());

        new RoundEndListener(teamService::getTeams, () -> {}).accept(new RoundEndEvent());

        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());
        assertTrue(bomberTeam.getPlayers().isEmpty());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndResetsSpectator(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), player);
        SpectatorHelper.enable(player);

        new RoundEndListener(teamService::getTeams, () -> {}).accept(new RoundEndEvent());

        assertFalse(SpectatorHelper.isSpectator(player));
        assertFalse(player.isAllowFlying());
        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndRemovesStaminaBars(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance);
        Player bomber = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), survivor);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);
        StaminaService staminaService = new StaminaService();
        staminaService.createStaminaObjects(teamService);
        staminaService.start();
        assertNotNull(staminaService.getStaminaBar(survivor));
        assertNotNull(staminaService.getStaminaBar(bomber));

        new RoundEndListener(teamService::getTeams, staminaService::cleanUp).accept(new RoundEndEvent());

        assertNull(staminaService.getStaminaBar(survivor));
        assertNull(staminaService.getStaminaBar(bomber));

        env.destroyInstance(instance, true);
    }
}
