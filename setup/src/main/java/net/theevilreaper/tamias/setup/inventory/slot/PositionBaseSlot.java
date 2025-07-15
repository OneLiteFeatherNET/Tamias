package net.theevilreaper.tamias.setup.inventory.slot;

import net.minestom.server.inventory.click.Click;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.aves.inventory.slot.Slot;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class PositionBaseSlot extends Slot {

    protected final Point point;
    protected final PlayerConsumer rightClickAction;

    PositionBaseSlot(@NotNull Point point, @NotNull PlayerConsumer rightClickAction) {
        this.point = point;
        this.rightClickAction = rightClickAction;
    }

    protected ClickHolder handleClick(
            @NotNull Player player,
            int ignore,
            @NotNull Click type
    ) {
        if (type instanceof Click.Left) {
            player.closeInventory();
            player.teleport(Pos.fromPoint(this.point));
            return ClickHolder.cancelClick();
        }

        if (type instanceof Click.Right && this.rightClickAction != null) {
            this.rightClickAction.accept(player);
        }

        return ClickHolder.cancelClick();
    }
}
