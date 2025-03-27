package net.theevilreaper.tamias.setup.listener.item;

import net.minestom.server.event.item.PickupItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerPickupItemListener implements Consumer<PickupItemEvent> {
    @Override
    public void accept(@NotNull PickupItemEvent event) {
        event.setCancelled(true);
    }
}
