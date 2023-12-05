package net.theevilreaper.tamias.listener.game;

import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.theevilreaper.tamias.config.GameConfig;
import net.theevilreaper.tamias.stamina.ExplodeBar;
import net.theevilreaper.tamias.stamina.StaminaBar;
import net.theevilreaper.tamias.team.TeamHelper;
import net.theevilreaper.tamias.util.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    private final TeamHelper teamHelper;

    private final Function<@NotNull Player, @Nullable StaminaBar> staminaMapper;

    public ProjectileEntityListener(@NotNull TeamHelper teamHelper, Function<@NotNull Player, @Nullable StaminaBar> staminaMapper) {
        this.teamHelper = teamHelper;
        this.staminaMapper = staminaMapper;
    }

    @Override
    public void accept(ProjectileCollideWithEntityEvent event) {
        event.getEntity().remove();
        var target = event.getTarget();

        if (!(target instanceof Player targetPlayer)) return;
        if (!target.hasTag(Tags.TEAM_ID)) return;

        byte teamValue = target.getTag(Tags.TEAM_ID);

        if (teamValue == GameConfig.TNT_ID) {
            ((ExplodeBar)staminaMapper.apply(targetPlayer)).explode();
            return;
        }

        this.teamHelper.removeSurvivor(targetPlayer);
        this.teamHelper.addTNT(targetPlayer);
    }
}
