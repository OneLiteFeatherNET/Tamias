package net.theevilreaper.tamias.team;

import de.icevizion.xerus.api.ColorData;
import de.icevizion.xerus.api.team.TeamImpl;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.util.Tags;
import org.jetbrains.annotations.NotNull;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class TamiasTeam extends TeamImpl {

    private byte teamTagId;

    TamiasTeam(@NotNull String name, @NotNull ColorData colorData, int initialCapacity) {
        super(name, colorData, initialCapacity);
    }

    public void setTeamTagId(byte teamTagId) {
        this.teamTagId = teamTagId;
    }

    @Override
    public void addPlayer(@NotNull Player paramPlayer) {
        super.addPlayer(paramPlayer);
        paramPlayer.setTag(Tags.TEAM_ID, teamTagId);
    }

    @Override
    public void removePlayer(@NotNull Player paramPlayer) {
        super.removePlayer(paramPlayer);
        paramPlayer.removeTag(Tags.TEAM_ID);
    }
}
