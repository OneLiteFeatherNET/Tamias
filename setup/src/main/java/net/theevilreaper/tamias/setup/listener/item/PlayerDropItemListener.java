package net.theevilreaper.tamias.setup.listener.item;

import net.minestom.server.event.item.ItemDropEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerDropItemListener implements Consumer<ItemDropEvent> {

    @Override
    public void accept(@NotNull ItemDropEvent event) {
         event.setCancelled(true);
    }
}
