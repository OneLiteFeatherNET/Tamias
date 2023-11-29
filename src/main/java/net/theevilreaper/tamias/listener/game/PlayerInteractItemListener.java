package net.theevilreaper.tamias.listener.game;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.theevilreaper.tamias.stamina.ExplodeBar;
import net.theevilreaper.tamias.stamina.ShootBar;
import net.theevilreaper.tamias.stamina.StaminaBar;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public class PlayerInteractItemListener implements Consumer<PlayerUseItemEvent> {

    private final Function<Void, StaminaBar> staminaBarConsumer;

    public PlayerInteractItemListener(Function<Void, StaminaBar> staminaBarConsumer) {
        this.staminaBarConsumer = staminaBarConsumer;
    }

    @Override
    public void accept(PlayerUseItemEvent event) {
        StaminaBar stamina = staminaBarConsumer.apply(null);

        if (stamina instanceof ExplodeBar explodeBar) {
            explodeBar.start();
        }

        if (stamina instanceof ShootBar shootBar) {
            shootBar.handleShoot();
        }
    }
}
