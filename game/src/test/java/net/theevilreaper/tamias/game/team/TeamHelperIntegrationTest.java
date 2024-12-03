package net.theevilreaper.tamias.game.team;

import de.icevizion.xerus.api.ColorData;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import de.icevizion.xerus.api.team.TeamServiceImpl;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TeamHelperIntegrationTest {

    private static TeamService<Team> teamService;

    @BeforeAll
    static void initTest() {
        teamService = new TeamServiceImpl<>();
        TamiasTeamCreator creator = new TamiasTeamCreator();
        teamService.add(Team.builder(creator).name(GameConfig.SURVIVOR_TEAM_NAME).capacity(16).colorData(ColorData.GOLD).build());
        teamService.add(Team.builder(creator).name(GameConfig.BOMBER_TEAM).capacity(16).colorData(ColorData.RED).build());
    }

    @AfterEach
    void tearDown() {
        for (Team team : teamService.getTeams()) {
            team.getPlayers().clear();
        }
    }

    @Test
    void testTamiasTeamAssert() {
        for (int i = 0; i < teamService.getTeams().size(); i++) {
            Team team = teamService.getTeams().get(i);
            assertInstanceOf(TamiasTeam.class, team);
        }
    }

    @Test
    void testTeamAllocationWithEmptyService() {
        TeamService<Team> emptyService = new TeamServiceImpl<>();
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> TeamHelper.allocateTeams(emptyService),
                "The team service must contain teams"
        );
    }

    @Test
    void testTeamAllocateWithInvalidPlayerBase(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        env.createPlayer(instance);

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> TeamHelper.allocateTeams(teamService),
                "The player list must contain at least two players"
        );

        env.destroyInstance(instance, true);
    }


    @Test
    void testAllocateTeams(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        List<Player> playerList = List.of(env.createPlayer(instance), env.createPlayer(instance));
        assertEquals(2, playerList.size());

        TeamHelper.allocateTeams(teamService);

        for (Team team : teamService.getTeams()) {
            assertFalse(team.isEmpty());
        }

        env.destroyInstance(instance, true);
    }

    @Test
    void testTeamTeleportWithEmptyTeams(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameMap map = new GameMap();
        Team tnt = teamService.getTeams().get(GameConfig.TNT_ID);
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> TeamHelper.teleport(tnt, map.getBomberInitialSpawn()),
                "The given team can't be empty"
        );

        Team survivorTeam = teamService.getTeams().get(GameConfig.SURVIVOR_ID);
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> TeamHelper.teleport(survivorTeam, map.getSpawn()),
                "The given team can't be empty"
        );

        env.destroyInstance(instance, true);
    }

    @Disabled(value = "Fix teleportation test later")
    @Test
    void testTeamTeleportation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameMap map = new GameMap();
        Pos survivorPos = new Pos(5, 10, 5);
        Pos bomberPos = new Pos(10, 10, 10);
        map.setBomberInitialSpawn(bomberPos);
        map.setSpawn(survivorPos);

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            players.add(env.createPlayer(instance));
        }

        TeamHelper.allocateTeams(teamService);

        Player tntPlayer = null;
        for (int i = 0; i < players.size() && tntPlayer == null; i++) {
            Player current = players.get(i);
            if (current.getTag(Tags.TEAM_ID) != GameConfig.TNT_ID) continue;
            tntPlayer = current;
        }

        assertNotNull(tntPlayer);

        TeamHelper.teleport(teamService.getTeams().getFirst(), map.getSpawn());

        assertEquals(bomberPos, tntPlayer.getPosition());

        teamService.getTeams().get(GameConfig.SURVIVOR_ID).getPlayers()
                .forEach(player -> assertEquals(survivorPos, player.getPosition()));

        env.destroyInstance(instance, true);
    }
}