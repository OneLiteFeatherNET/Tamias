package net.theevilreaper.tamias.game.team;

import de.icevizion.xerus.api.ColorData;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamCreator;
import net.theevilreaper.tamias.game.config.GameConfig;
import org.jetbrains.annotations.NotNull;

public final class TamiasTeamCreator implements TeamCreator {

    @Override
    public @NotNull Team createTeam(@NotNull String name, @NotNull ColorData colorData, int i) {
        TamiasTeam team = new TamiasTeam(name, colorData, i);
        team.setTeamTagId("Survivor".equals(name) ? GameConfig.SURVIVOR_ID : GameConfig.TNT_ID);
        return team;
    }
}
