package net.theevilreaper.tamias.game.listener.game;

import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.common.util.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    private final PlayerConsumer teamUpdater;
    private final Function<@NotNull UUID, @Nullable StaminaBar> staminaMapper;

    public ProjectileEntityListener(@NotNull PlayerConsumer teamUpdater, Function<@NotNull UUID, @Nullable StaminaBar> staminaMapper) {
        this.teamUpdater = teamUpdater;
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
            staminaBar.triggerAction();
            return;
        }
        // TODO: Fix
       // this.teamHelper.removeSurvivor(targetPlayer);
        //this.teamHelper.addTNT(targetPlayer);
    }
}
