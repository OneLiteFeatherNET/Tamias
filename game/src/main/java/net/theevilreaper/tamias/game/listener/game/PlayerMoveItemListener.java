package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.theevilreaper.tamias.game.util.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class PlayerMoveItemListener implements Consumer<InventoryPreClickEvent> {
    @Override
    public void accept(@NotNull InventoryPreClickEvent event) {
        if (event.getClickedItem().hasTag(Tags.ITEM_TAG) && event.getClickedItem().getTag(Tags.ITEM_TAG) == (byte) 0x01) {
            event.setCancelled(true);
        }
    }
}
