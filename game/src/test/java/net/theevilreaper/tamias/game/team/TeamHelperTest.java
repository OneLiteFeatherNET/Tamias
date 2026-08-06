package net.theevilreaper.tamias.game.team;

import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TeamHelperTest {

    private static TeamService teamService;

    @BeforeAll
    static void initTest() {
        teamService = TeamService.of();
    }

    @AfterEach
    void tearDown() {
        for (Team team : teamService.getTeams()) {
            team.getPlayers().clear();
        }
    }

    @Test
    void testTeamAssert() {
        TeamHelper.loadTeams(1, teamService);
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
}
