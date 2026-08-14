package net.theevilreaper.tamias.setup.inventory.slot;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.tamias.setup.event.DirectionSetEvent;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.theevilreaper.tamias.setup.util.SetupMessages.CYCLE_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.NO_SPACE_SEPARATOR;

public class DirectionSlot extends AbstractDataSlot {

    private static final Direction[] CYCLE = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private final @Nullable Direction direction;

    public DirectionSlot(MapDataCategory category, @Nullable Direction direction) {
        super(category);
        this.direction = direction;
    }

    private static Direction next(@Nullable Direction current) {
        if (current == null) return CYCLE[0];
        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i] == current) return CYCLE[(i + 1) % CYCLE.length];
        }
        return CYCLE[0];
    }

    @Override
    public ItemStack getItem() {
        ItemStack overviewItem = MapDataCategory.getDefaultItem(type);

        if (direction == null) {
            return overviewItem;
        }
        return overviewItem.with(builder -> builder.lore(
                Component.empty(),
                NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text(direction.name(), type.getColor())),
                Component.empty(),
                CYCLE_CLICK,
                Component.empty()
        ));
    }

    @Override
    protected void click(Player player, int slot, Click click, ItemStack stack, Consumer<ClickHolder> result) {
        result.accept(ClickHolder.cancelClick());

        if (click instanceof Click.Left) {
            EventDispatcher.call(new DirectionSetEvent(player, next(direction), type));
        }
    }
}
