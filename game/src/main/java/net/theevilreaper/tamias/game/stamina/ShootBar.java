package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.game.util.Items;
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
    static final int MAX_TIME = 11;
    private static final int PERIOD_MILLIS = 500;
    // The bar is ready again on the consume after MAX_TIME, a server tick takes 50 ms
    static final int COOLDOWN_TICKS = (MAX_TIME + 1) * PERIOD_MILLIS / 50;
    private int currentTime;

    /**
     * Creates a new reference from an {@link StaminaBar}.
     *
     * @param player the player who owns the bar
     */
    ShootBar(Player player) {
        super(player, ChronoUnit.MILLIS, PERIOD_MILLIS);
        this.currentTime = MAX_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        this.status = Status.READY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRegenerated() {
        player.playSound(LEVEL_UP);
        status = Status.READY;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ignored while the bar regenerates, so a survivor can't shoot again before the cooldown is over.
     */
    @Override
    public void triggerAction() {
        if (status == Status.REGENERATING) return;
        this.currentTime = 0;
        this.status = Status.REGENERATING;
        Items.setShootCooldown(player, COOLDOWN_TICKS);
        ProjectileHelper.spawnProjectile(player, ProjectileHelper::createProjectile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void consume() {
        if (status != Status.REGENERATING) return;
        if (currentTime == MAX_TIME) {
            this.onRegenerated();
            return;
        }
        this.currentTime += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ShootBar shootBar = (ShootBar) o;
        return currentTime == shootBar.currentTime;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), currentTime);
    }
}
