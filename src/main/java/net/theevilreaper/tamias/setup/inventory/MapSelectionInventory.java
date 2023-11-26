package net.theevilreaper.tamias.setup.inventory;

import de.icevizion.aves.inventory.GlobalInventoryBuilder;
import de.icevizion.aves.inventory.InventoryLayout;
import de.icevizion.aves.inventory.InventorySlot;
import de.icevizion.aves.inventory.util.InventoryConstants;
import de.icevizion.aves.inventory.util.LayoutCalculator;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.inventory.condition.InventoryConditionResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MapSelectionInventory extends GlobalInventoryBuilder {

    private static final Material SLOT_ICON = Material.PAPER;
    private static final int[] SLOTS = LayoutCalculator.quad(InventoryType.CHEST_1_ROW.getSize() + 1, InventoryType.CHEST_5_ROW.getSize());

    public MapSelectionInventory(@NotNull Component title, @NotNull List<Path> maps, @NotNull BiFunction<ClickType, Path, Void> creationConsumer) {
        super(title, InventoryType.CHEST_6_ROW);
        var layout = new InventoryLayout(getType());
        var decoration = new InventorySlot(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE), InventoryConstants.CANCEL_CLICK);

        layout.setItems(LayoutCalculator.fillRow(InventoryType.CHEST_1_ROW), decoration);
        layout.setItems(LayoutCalculator.fillRow(InventoryType.CHEST_6_ROW), decoration);

        var dataLayout = new InventoryLayout(getType());

        setDataLayoutFunction(dataLayoutFunction -> {
            if (maps.isEmpty()) return dataLayout;
            dataLayout.blank(SLOTS);

            for (int i = 0; i < SLOTS.length; i++) {
                var entry = maps.get(i);
                var name = entry.getFileName().toString();
                var item = ItemStack.builder(SLOT_ICON).displayName(Component.text(name)).build();
                dataLayout.setItem(i, item, (player, clickType, i1, result) -> {
                    result.setCancel(true);
                    if (!(clickType == ClickType.LEFT_CLICK || clickType == ClickType.RIGHT_CLICK)) return;
                    creationConsumer.apply(clickType, entry);
                });
            }

            return dataLayout;
        });

        setLayout(layout);
        register();
    }

    public void open(@NotNull Player player) {
        player.openInventory(getInventory());
    }
}
