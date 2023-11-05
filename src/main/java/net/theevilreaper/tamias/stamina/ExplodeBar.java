package net.theevilreaper.tamias.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class ExplodeBar extends StaminaBar {

    private static final Sound TICK_SOUND = Sound.sound(SoundEvent.UI_BUTTON_CLICK, Sound.Source.MASTER, 1F, 10F);
    private static final Sound EXPLODE_SOUND = Sound.sound(SoundEvent.ENTITY_GENERIC_EXPLODE, Sound.Source.MASTER, 1F, 0F);
    private static final float MAX = 10;

    private float current;
    /**
     * Creates a new reference from an {@link StaminaBar}.
     *
     * @param player     the player who owns the bar
     */
    public ExplodeBar(@NotNull Player player) {
        super(player, ChronoUnit.MILLIS, 250);
    }

    @Override
    protected void onStart() {
        this.current = MAX + 1;
        this.player.setLevel((int) MAX);
        this.player.setExp(normalize(current - 1));
    }

    @Override
    protected void onRegenerated() {

    }

    @Override
    public void consume() {
        --current;
        this.player.setExp(normalize(current));
        this.player.setLevel(player.getLevel() - 1);

        if (current == 0) {
            var pos = Pos.fromPoint(player.getPosition());
            System.out.println("BOOM");
            //    player.getInstance().explode((float) pos.x(), (float) pos.y(), (float) pos.z(), 5f);
            this.stop();
            player.playSound(EXPLODE_SOUND);
            return;
        }

        player.playSound(TICK_SOUND);
    }
    private float normalize(float current) {
        if (current < 0) return 0;
        return (current) / MAX;
    }
}
