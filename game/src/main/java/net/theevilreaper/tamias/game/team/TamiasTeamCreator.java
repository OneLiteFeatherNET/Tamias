package net.theevilreaper.tamias.game.team;

import de.icevizion.xerus.api.ColorData;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamCreator;
import net.theevilreaper.tamias.common.config.GameConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Creates a new instance of the {@link TamiasTeam} instead of the default implementation.
 * The {@link TamiasTeam} is a implementation of the general {@link Team} implementation but add some additional code
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see TeamCreator
 * @since 1.0.0
 */
public final class TamiasTeamCreator implements TeamCreator {

    /**
     * Creates a new instance of the {@link TamiasTeam}.
     *
     * @param name      the name of the team
     * @param colorData the color of the team
     * @param i         the initial capacity of the team
     * @return the created team
     */
    @Override
    public @NotNull Team createTeam(@NotNull String name, @NotNull ColorData colorData, int i) {
        TamiasTeam team = new TamiasTeam(name, colorData, i);
        team.setTeamTagId(GameConfig.SURVIVOR_TEAM_NAME.equals(name) ? GameConfig.SURVIVOR_ID : GameConfig.TNT_ID);
        return team;
    }
}
