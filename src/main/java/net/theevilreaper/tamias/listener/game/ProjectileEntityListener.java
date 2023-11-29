package net.theevilreaper.tamias.listener.game;

import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.theevilreaper.tamias.util.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    private final TeamService<Team> teamService;

    public ProjectileEntityListener(@NotNull TeamService<Team> teamService) {
        this.teamService = teamService;
    }

    @Override
    public void accept(ProjectileCollideWithEntityEvent event) {
        var target = event.getTarget();

        if (!(target instanceof Player targetPlayer)) return;

        if (!target.hasTag(Tags.TEAM_ID) || target.getTag(Tags.TEAM_ID) == (byte) 0x01) return;

        event.getEntity().remove();
    }
}
