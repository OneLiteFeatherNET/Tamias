package net.theevilreaper.tamias.game.listener.game;

import ca.atlasengine.projectiles.AbstractProjectile;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lets a bomber explode immediately when an egg hits them. Hits on any other entity have no effect.
 * Eggs fly through spectators.
 */
public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    private final Function<UUID, @Nullable StaminaBar> staminaMapper;

    public ProjectileEntityListener(Function<UUID, @Nullable StaminaBar> staminaMapper) {
        this.staminaMapper = staminaMapper;
    }

    @Override
    public void accept(ProjectileCollideWithEntityEvent event) {
        // A cancelled collision keeps the projectile alive, so it flies through the spectator
        if (event.getTarget() instanceof Player player && SpectatorHelper.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        event.getEntity().remove();

        if (!(event.getTarget() instanceof Player target)) return;
        if (!GameConfig.BOMBER_KEY.asString().equals(target.getTag(Tags.TEAM_KEY))) return;
        if (!(staminaMapper.apply(target.getUuid()) instanceof ExplodeBar explodeBar)) return;

        if (event.getEntity() instanceof AbstractProjectile projectile && projectile.getShooter() instanceof TamiasPlayer shooter) {
            shooter.addHit();
        }
        explodeBar.explode();
    }
}
