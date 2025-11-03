package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.game.util.ProjectileHelper;

import java.time.temporal.ChronoUnit;

/**
 * The {@link ShootBar} is a implementation of the {@link StaminaBar} which used from the survivor team to shoot eggs on the bomber team.
 * If a player shoots an egg, the bar will switch to a regeneration phase. The player can only shoot again if the bar is full.
 * That means a player needs to shoot with a bit of strategy to hit the bomber.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class ShootBar extends StaminaBar {

    private static final Sound LEVEL_UP = Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1F, 1F);
    private static final int MAX_TIME = 16;
    private int currentTime;

    /**
     * Creates a new reference from an {@link StaminaBar}.
     *
     * @param player the player who owns the bar
     */
    ShootBar(Player player) {
        super(player, ChronoUnit.MILLIS, 500);
        this.currentTime = MAX_TIME;
    }

    /**
     * Called when the {@link StaminaBar} is started in general.
     */
    @Override
    protected void onStart() {
        this.status = Status.READY;
    }

    /**
     * The method can be called when the regeneration of the {@link StaminaBar} is finished.
     */
    @Override
    protected void onRegenerated() {
        player.playSound(LEVEL_UP);
        status = Status.READY;
    }

    @Override
    public void triggerAction() {
        this.currentTime = 0;
        this.status = Status.REGENERATING;
        ProjectileHelper.spawnProjectile(player, ProjectileHelper::createProjectile);
    }

    @Override
    public void consume() {
        if (status != Status.REGENERATING) return;
        if (currentTime == MAX_TIME) {
            this.onRegenerated();
            return;
        }
        this.currentTime += 1;
    }
}
