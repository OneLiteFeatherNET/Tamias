package net.theevilreaper.tamias.game.listener.game;

import ca.atlasengine.projectiles.AbstractProjectile;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lets a bomber explode immediately when an egg hits them. Hits on any other entity have no effect.
 * Eggs fly through spectators. The shooter hears a hit sound and the bomber gets told who shot them.
 */
public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    static final Sound HIT_SOUND = Sound.sound(SoundEvent.ENTITY_ARROW_HIT_PLAYER, Sound.Source.MASTER, 1f, 1f);

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

        if (!explodeBar.explode()) return;
        if (!(event.getEntity() instanceof AbstractProjectile projectile && projectile.getShooter() instanceof Player shooter)) return;
        shooter.playSound(HIT_SOUND);
        target.sendMessage(GameMessages.getShotMessage(shooter));
        if (shooter instanceof TamiasPlayer tamiasShooter) {
            tamiasShooter.addHit();
        }
    }
}
