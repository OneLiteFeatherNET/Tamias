package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.time.TimeUnit;
import net.theevilreaper.tamias.game.util.ProjectileHelper;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class ShootBar extends StaminaBar {

    private static final Sound LEVEL_UP = Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1F, 1F);
    private static final int MAX_TIME = 16;

    private int currentTime;

    /**
     * Creates a new reference from an {@link StaminaBar}.
     *
     * @param player     the player who owns the bar
     */
    public ShootBar(@NotNull Player player) {
        super(player, ChronoUnit.MILLIS, 500);
        this.currentTime = MAX_TIME;
    }

    @Override
    protected void onStart() {
        // Nothing to do
    }

    @Override
    protected void onRegenerated() {
        player.playSound(LEVEL_UP);
    }

    @Override
    public void consume() {
        if (status != Status.REGENERATING) return;
        if (currentTime == MAX_TIME) {
            status = Status.READY;
            return;
        }
        this.currentTime += 1;
    }

    public void handleShoot() {
        this.currentTime = 0;
        this.status = Status.REGENERATING;
        ProjectileHelper.spawnProjectile(player, this::createProjectile);
    }

    private @NotNull EntityProjectile createProjectile(@NotNull Player player) {
        var projectile = new EntityProjectile(player, EntityType.EGG);
        var eggMeta = (ThrownEggMeta) projectile.getEntityMeta();
        eggMeta.setOnFire(true);

        projectile.scheduleRemove(Duration.of(100, TimeUnit.SERVER_TICK));
        return projectile;
    }
}
