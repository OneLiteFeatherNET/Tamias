package net.theevilreaper.tamias.game.team;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.xerus.api.ColorData;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeamHelperTest extends TamiasPlayerTestBase {

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
    void testReplacementBomberIsAChosenSurvivor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Player firstSurvivor = env.createPlayer(instance);
        Player secondSurvivor = env.createPlayer(instance);
        TeamHelper.addPlayerToTeam(survivorTeam, firstSurvivor);
        TeamHelper.addPlayerToTeam(survivorTeam, secondSurvivor);

        Player replacement = TeamHelper.chooseReplacementBomber(teamService).orElseThrow();

        assertTrue(replacement == firstSurvivor || replacement == secondSurvivor);
        env.destroyInstance(instance, true);
    }

    @Test
    void testNoReplacementBomberWhileABomberIsLeft(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), env.createPlayer(instance));
        TeamHelper.addPlayerToTeam(survivorTeam, env.createPlayer(instance));
        TeamHelper.addPlayerToTeam(survivorTeam, env.createPlayer(instance));

        assertTrue(TeamHelper.chooseReplacementBomber(teamService).isEmpty());
        env.destroyInstance(instance, true);
    }

    @Test
    void testNoReplacementBomberForTheLastSurvivor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, env.createPlayer(instance));

        assertTrue(TeamHelper.chooseReplacementBomber(teamService).isEmpty());
        env.destroyInstance(instance, true);
    }

    @Test
    void testReplaceMissingBomberConvertsASurvivor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, env.createPlayer(instance));
        TeamHelper.addPlayerToTeam(survivorTeam, env.createPlayer(instance));

        TeamHelper.replaceMissingBomber(teamService);

        assertEquals(1, bomberTeam.getPlayers().size());
        assertEquals(1, survivorTeam.getPlayers().size());
        Player newBomber = bomberTeam.getPlayers().iterator().next();
        assertEquals(GameConfig.BOMBER_KEY.asString(), newBomber.getTag(Tags.TEAM_KEY));
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) newBomber).getDisguiseState());
        env.destroyInstance(instance, true);
    }

    @Test
    void testTeamColorIsAppliedToTheDisplayName(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();

        TeamHelper.addPlayerToTeam(survivorTeam, player);
        assertEquals(Component.text(player.getUsername(), ColorData.GREEN.getChatColor()), player.getDisplayName());

        TeamHelper.removePlayerFromTeam(survivorTeam, player);
        assertEquals(Component.text(player.getUsername()), player.getDisplayName());

        env.destroyInstance(instance, true);
    }

    @Test
    void testConvertedSurvivorShowsTheBomberColor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), player);

        TeamHelper.switchToTNTTeam(teamService, player);

        assertEquals(Component.text(player.getUsername(), ColorData.RED.getChatColor()), player.getDisplayName());
        env.destroyInstance(instance, true);
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
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());

        TeamHelper.removePlayerFromTeam(bomberTeam, player);
        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());

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
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());

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
