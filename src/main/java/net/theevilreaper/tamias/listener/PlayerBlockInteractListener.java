package net.theevilreaper.tamias.listener;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class PlayerBlockInteractListener implements Consumer<PlayerBlockInteractEvent> {
    @Override
    public void accept(@NotNull PlayerBlockInteractEvent event) {
        event.setBlockingItemUse(true);
        event.setCancelled(true);
    }
}
