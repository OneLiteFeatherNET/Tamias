package net.theevilreaper.tamias.game.team;

import net.theevilreaper.xerus.api.ColorData;
import net.theevilreaper.xerus.api.team.TeamImpl;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class TamiasTeam extends TeamImpl {

    private byte teamTagId;

    /**
     * Creates a new instance from the team.
     *
     * @param name            the name which the team should have
     * @param colorData       the {@link ColorData} which the team should have
     * @param initialCapacity the capacity of the team
     */
    TamiasTeam(@NotNull String name, @NotNull ColorData colorData, int initialCapacity) {
        super(name, colorData, initialCapacity);
    }

    /**
     * Set's the team id which is required for the game.
     *
     * @param teamTagId the id to set
     */
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
        paramPlayer.getInventory().clear();
        if (paramPlayer.getEntityType() != EntityType.PLAYER) {
            paramPlayer.switchEntityType(EntityType.PLAYER);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TamiasTeam that = (TamiasTeam) o;
        return teamTagId == that.teamTagId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamTagId);
    }
}
