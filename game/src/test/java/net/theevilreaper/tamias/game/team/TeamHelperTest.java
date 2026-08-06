package net.theevilreaper.tamias.game.team;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TeamHelperTest {

    private TeamService teamService;

    @BeforeEach
    void initTest() {
        teamService = TeamService.of();
        TeamHelper.loadTeams(4, teamService);
    }

    @AfterEach
    void tearDown() {
        for (Team team : teamService.getTeams()) {
            team.getPlayers().clear();
        }
    }

    @Test
    void testTeamAssert() {
        assertEquals(2, teamService.getTeams().size());
        for (Team team : teamService.getTeams()) {
            assertNotNull(team.key());
        }
    }

    @Test
    void testTeamAllocationWithEmptyService() {
        TeamService emptyService = TeamService.of();
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> TeamHelper.allocateTeams(emptyService),
                "The team service must contain teams"
        );
    }

    @Test
    void testAddAndRemovePlayerFromTeam(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, player);

        assertTrue(survivorTeam.getPlayers().contains(player));
        assertTrue(player.hasTag(Tags.TEAM_KEY));
        assertEquals(GameConfig.SURVIVOR_KEY.asString(), player.getTag(Tags.TEAM_KEY));
        assertTrue(player.getInventory().getItemStacks().length > 0);

        TeamHelper.removePlayerFromTeam(survivorTeam, player);
        assertFalse(survivorTeam.getPlayers().contains(player));
        assertFalse(player.hasTag(Tags.TEAM_KEY));

        env.destroyInstance(instance, true);
    }

    @Test
    void testAddPlayerToBomberTeamSwitchesEntity(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, player);

        assertTrue(bomberTeam.getPlayers().contains(player));
        assertEquals(EntityType.TNT, player.getEntityType());

        TeamHelper.removePlayerFromTeam(bomberTeam, player);
        assertEquals(EntityType.PLAYER, player.getEntityType());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSwitchToTNTTeam(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();

        TeamHelper.addPlayerToTeam(survivorTeam, player);
        assertTrue(survivorTeam.getPlayers().contains(player));

        TeamHelper.switchToTNTTeam(teamService, player);

        assertFalse(survivorTeam.getPlayers().contains(player));
        assertTrue(bomberTeam.getPlayers().contains(player));
        assertEquals(GameConfig.BOMBER_KEY.asString(), player.getTag(Tags.TEAM_KEY));
        assertEquals(EntityType.TNT, player.getEntityType());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSwitchToTNTTeamWithoutTagThrowsException(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> TeamHelper.switchToTNTTeam(teamService, player)
        );

        env.destroyInstance(instance, true);
    }
}
