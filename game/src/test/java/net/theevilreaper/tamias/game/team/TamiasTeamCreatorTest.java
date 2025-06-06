package net.theevilreaper.tamias.game.team;

import net.theevilreaper.xerus.api.ColorData;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.tamias.common.config.GameConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TamiasTeamCreatorTest {

    @Test
    void testTamiasTeamCreator() {
        Team.Builder builder = Team.builder(new TamiasTeamCreator());
        assertNotNull(builder);
        Team team = builder.name(GameConfig.BOMBER_TEAM).capacity(10).colorData(ColorData.RED).build();
        assertNotNull(team);
        assertInstanceOf(TamiasTeam.class, team);
    }
}
