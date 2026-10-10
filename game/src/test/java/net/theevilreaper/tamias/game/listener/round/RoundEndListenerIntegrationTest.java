package net.theevilreaper.tamias.game.listener.round;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        new RoundEndListener(teamService::getTeams).accept(new RoundEndEvent());

        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());
        assertTrue(bomberTeam.getPlayers().isEmpty());

        env.destroyInstance(instance, true);
    }
}
