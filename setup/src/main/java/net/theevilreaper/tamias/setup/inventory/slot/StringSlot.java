package net.theevilreaper.tamias.setup.inventory.slot;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.tamias.setup.dialog.event.DialogRequestEvent;
import net.theevilreaper.tamias.setup.dialog.event.DialogTarget;
import net.theevilreaper.tamias.setup.event.PlayerRemoveDataEvent;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

import java.util.function.Consumer;

import static net.theevilreaper.tamias.setup.util.SetupMessages.DELETE_CLICK;
import static net.theevilreaper.tamias.setup.util.SetupMessages.NO_SPACE_SEPARATOR;

public class StringSlot extends AbstractDataSlot {

    private final String data;

    public StringSlot(MapDataCategory category, String data) {
        super(category);
        this.data = data;
    }

    @Override
    public ItemStack getItem() {
        ItemStack overviewItem = MapDataCategory.getDefaultItem(type);

        if (data.equals("Map")) return overviewItem;
        return asBuilder(overviewItem).lore(
                        Component.empty(),
                        NO_SPACE_SEPARATOR.append(Component.space()).append(Component.text(data, type.getColor())),
                        Component.empty(),
                        DELETE_CLICK,
                        Component.empty()
                )
                .build();
    }

    @Override
    protected void click(Player player, int slot, Click click, ItemStack stack, Consumer<ClickHolder> result) {
        result.accept(ClickHolder.cancelClick());

        if (data.equals("Map")) {
            EventDispatcher.call(new DialogRequestEvent(player, DialogTarget.CREATE_NAME));
            return;
        }

        if (click instanceof Click.Right) {
            EventDispatcher.call(new PlayerRemoveDataEvent(player, type));
        }
    }
}
