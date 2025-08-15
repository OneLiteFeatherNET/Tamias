package net.theevilreaper.tamias.game.team;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.xerus.api.team.DefaultTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class TamiasTeam extends DefaultTeam {

    private final byte teamTagId;

    /**
     * Creates a new instance from the team.
     *
     * @param name            the name which the team should have
     */
    TamiasTeam(@NotNull Key name, int initialCapacity, byte teamTagId) {
        super(name, initialCapacity);
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
