package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.theevilreaper.tamias.game.config.GameConfig;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    private final TeamHelper teamHelper;

    private final Function<@NotNull UUID, @Nullable StaminaBar> staminaMapper;

    public ProjectileEntityListener(@NotNull TeamHelper teamHelper, Function<@NotNull UUID, @Nullable StaminaBar> staminaMapper) {
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
        var staminaBar = staminaMapper.apply(targetPlayer.getUuid());

        if (staminaBar == null) return;

        if (teamValue == GameConfig.TNT_ID) {
            ((ExplodeBar)staminaBar).explode();
            return;
        }
        this.teamHelper.removeSurvivor(targetPlayer);
        this.teamHelper.addTNT(targetPlayer);
    }
}
