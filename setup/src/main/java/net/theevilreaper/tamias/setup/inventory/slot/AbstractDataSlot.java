package net.theevilreaper.tamias.setup.inventory.slot;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.slot.Slot;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

import java.util.function.Consumer;

public abstract class AbstractDataSlot extends Slot {

    protected final MapDataCategory type;

    protected AbstractDataSlot(MapDataCategory type) {
        this.type = type;
        this.setClick(this::click);
    }

    protected abstract void click(
            Player player,
            int slot,
            Click clickType,
            ItemStack stack,
            Consumer<ClickHolder> result
    );
}
