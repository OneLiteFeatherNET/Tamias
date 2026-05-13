package net.theevilreaper.tamias.setup.inventory.slot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.slot.Slot;
import net.theevilreaper.tamias.setup.dialog.event.PlayerDialogRequestEvent;

public final class EmptyItemSlot extends Slot {

    private static final ItemStack STACK = ItemStack.builder(Material.BARRIER)
            .customName(Component.text("No data set!", NamedTextColor.RED))
            .build();

    /**
     * Creates a new EmptyItemSlot that requests the given data type on click.
     *
     * @param target the data type to request on click
     */
    public EmptyItemSlot(PlayerDialogRequestEvent.Target target) {
        super((player, _, _, _, result) -> {
            result.accept(ClickHolder.cancelClick());
            EventDispatcher.call(new PlayerDialogRequestEvent(player, target));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack getItem() {
        return STACK;
    }
}
