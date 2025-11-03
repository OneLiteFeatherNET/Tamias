package net.theevilreaper.tamias.setup.inventory.slot;

import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.slot.Slot;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

import java.util.function.Consumer;

public abstract class PositionBaseSlot extends Slot {

    protected final Point point;
    protected final PlayerConsumer rightClickAction;

    PositionBaseSlot(Point point, PlayerConsumer rightClickAction) {
        this.point = point;
        this.rightClickAction = rightClickAction;
    }

    protected void handleClick(
            Player player,
            int slot,
            Click click,
            ItemStack stack,
            Consumer<ClickHolder> result
    ) {
        result.accept(ClickHolder.cancelClick());
        if (click instanceof Click.Left) {
            player.closeInventory();
            player.teleport(this.point.asPos());
            return;
        }

        if (click instanceof Click.Right && this.rightClickAction != null) {
            this.rightClickAction.accept(player);
            return;
        }
    }
}
