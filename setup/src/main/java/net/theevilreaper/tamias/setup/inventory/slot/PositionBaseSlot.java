package net.theevilreaper.tamias.setup.inventory.slot;

import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.slot.Slot;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class PositionBaseSlot extends Slot {

    protected final Point point;
    protected final PlayerConsumer rightClickAction;

    PositionBaseSlot(@NotNull Point point, @NotNull PlayerConsumer rightClickAction) {
        this.point = point;
        this.rightClickAction = rightClickAction;
    }

    protected void handleClick(
            @NotNull Player player,
            int slot,
            @NotNull Click click,
            @NotNull ItemStack stack,
            @NotNull Consumer<ClickHolder> result
    ) {
        result.accept(ClickHolder.cancelClick());
        if (click instanceof Click.Left) {
            player.closeInventory();
            player.teleport(Pos.fromPoint(this.point));
            return;
        }

        if (click instanceof Click.Right && this.rightClickAction != null) {
            this.rightClickAction.accept(player);
            return;
        }
    }
}
