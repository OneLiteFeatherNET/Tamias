package net.theevilreaper.tamias.setup.inventory.slot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.theevilreaper.aves.inventory.click.ClickHolder;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

import java.util.function.Consumer;

import static net.theevilreaper.tamias.setup.util.SetupMessages.NO_SPACE_SEPARATOR;
import static net.theevilreaper.tamias.setup.util.SetupMessages.OPEN_CLICK;

/**
 * Displays how many ground presets a map has and opens the preset overview on a left click.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public class GroundPresetsSlot extends AbstractDataSlot {

    private final int presetCount;
    private final Runnable openOverview;

    public GroundPresetsSlot(int presetCount, Runnable openOverview) {
        super(MapDataCategory.GROUND_PRESETS);
        this.presetCount = presetCount;
        this.openOverview = openOverview;
    }

    @Override
    public ItemStack getItem() {
        Component state = presetCount == 0
                ? Component.text("Global defaults", NamedTextColor.GRAY)
                : Component.text(presetCount + (presetCount == 1 ? " preset" : " presets"), type.getColor());
        return MapDataCategory.getDefaultItem(type).with(builder -> builder.lore(
                Component.empty(),
                NO_SPACE_SEPARATOR.append(Component.space()).append(state),
                Component.empty(),
                OPEN_CLICK,
                Component.empty()
        ));
    }

    @Override
    protected void click(Player player, int slot, Click click, ItemStack stack, Consumer<ClickHolder> result) {
        result.accept(ClickHolder.cancelClick());

        if (click instanceof Click.Left) {
            this.openOverview.run();
        }
    }
}
