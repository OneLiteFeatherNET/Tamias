package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

/**
 * The {@link ExplodeBar} is a implementation of the {@link StaminaBar} which is used from the bomber team to explode the player.
 * If the bar is empty, the player will explode and the after a certain time the player receives a new spawn position and the bar resets
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
@SuppressWarnings("java:S3252")
public final class ExplodeBar extends StaminaBar {

    private static final Sound TICK_SOUND = Sound.sound(SoundEvent.UI_BUTTON_CLICK, Sound.Source.MASTER, 1F, 10F);
    private static final Sound EXPLODE_SOUND = Sound.sound(SoundEvent.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 1F, 1F);
    private static final Potion BLINDNESS = new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE);
    private static final float MAX = 10;

    private float current;

    /**
     * Creates a new reference from an {@link StaminaBar}.
     *
     * @param player the player who owns the bar
     */
    ExplodeBar(@NotNull Player player) {
        super(player, ChronoUnit.MILLIS, 250);
    }

    @Override
    protected void onStart() {
        this.resetToDefaults();
    }

    @Override
    protected void onRegenerated() {
        EventDispatcher.call(new BomberRequireSpawnEvent(player, this));
    }

    public void resetToDefaults() {
        this.current = MAX + 1;
        this.player.setLevel((int) MAX);
        this.player.setExp(normalize(current - 1));
        this.status = Status.READY;
    }

    @Override
    public void triggerAction() {
        status = Status.DRAINING;
    }

    @Override
    public void consume() {
        if (status == null || status == Status.READY) return;

        if (status == Status.DRAINING) {
            this.handleBomberTick();
            return;
        }

        this.handleBomberRegeneration();
    }

    /**
     * Handles the logic when a bomber wants to explode.
     */
    private void handleBomberTick() {
        --current;
        this.player.setExp(normalize(current));
        this.player.setLevel(player.getLevel() - 1);
        if (current > 0) {
            player.playSound(TICK_SOUND);
            return;
        }
        this.explode();
        player.playSound(EXPLODE_SOUND);
        this.status = Status.REGENERATING;
    }

    private void handleBomberRegeneration() {
        ++current;

        if (current > MAX / 2) {
            this.onRegenerated();
        }
    }

    /**
     * Triggers the logic to explode a player.
     */
    public void explode() {
        Instance instance = this.player.getInstance();
        Pos pos = Pos.fromPoint(player.getPosition());
        instance.explode((float) pos.x(), (float) pos.y(), (float) pos.z(), 1);
        player.addEffect(BLINDNESS);
        AttributeHelper.disableMovement(player);
    }

    /**
     * Normalizes the current value to a float value between 0 and 1.
     * @param current the current value
     * @return the normalized value
     */
    private float normalize(float current) {
        return current < 0 ? 0 : current / MAX;
    }
}
