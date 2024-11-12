package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.ShootBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.util.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PlayerInteractItemListener implements Consumer<PlayerUseItemEvent> {

    private final Function<Void, StaminaBar> staminaBarConsumer;

    public PlayerInteractItemListener(Function<Void, StaminaBar> staminaBarConsumer) {
        this.staminaBarConsumer = staminaBarConsumer;
    }

    @Override
    public void accept(@NotNull PlayerUseItemEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag(Tags.ITEM_TAG)) return;
        final byte value = stack.getTag(Tags.ITEM_TAG);
        if (value > 0x02) return;

        StaminaBar stamina = staminaBarConsumer.apply(null);
        if (stamina instanceof ExplodeBar explodeBar) {
            explodeBar.start();
        }

        if (stamina instanceof ShootBar) {
            stamina.triggerAction();
        }
    }
}
