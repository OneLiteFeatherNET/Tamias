package net.theevilreaper.tamias.setup.inventory;

import de.icevizion.aves.inventory.GlobalInventoryBuilder;
import de.icevizion.aves.inventory.InventoryLayout;
import de.icevizion.aves.inventory.InventorySlot;
import de.icevizion.aves.inventory.util.InventoryConstants;
import de.icevizion.aves.inventory.util.LayoutCalculator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.event.MapSelectionEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("java:S3252")
public final class MapSelectionInventory extends GlobalInventoryBuilder {

    private static final Material SLOT_ICON = Material.PAPER;
    private static final int[] SLOTS = LayoutCalculator.repeat(InventoryType.CHEST_1_ROW.getSize(), InventoryType.CHEST_5_ROW.getSize());

    public MapSelectionInventory(@NotNull Component title, @NotNull List<Path> maps, @NotNull BiFunction<ClickType, Path, Void> creationConsumer) {
        super(title, InventoryType.CHEST_6_ROW);
        var layout = InventoryLayout.fromType(getType());
        var decoration = new InventorySlot(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE), InventoryConstants.CANCEL_CLICK);

        layout.setItems(LayoutCalculator.fillRow(InventoryType.CHEST_1_ROW), decoration);
        layout.setItems(LayoutCalculator.fillRow(InventoryType.CHEST_6_ROW), decoration);

        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        //lore.add(Messages.withMini("<gray>Left-click: <yellow>Lobby <gray>setup"));
        lore.add(Component.empty());
        //lore.add(Messages.withMini("<gray>Right-click: <red>Game <gray>setup"));

        setDataLayoutFunction(dataLayoutFunction -> {
            var dataLayout = dataLayoutFunction == null ? InventoryLayout.fromType(getType()) : dataLayoutFunction;
            if (maps.isEmpty()) return dataLayout;
            dataLayout.blank(SLOTS);
            for (int i = 0; i < maps.size() && i < SLOTS.length; i++) {
                var entry = maps.get(i);
                var name = entry.getFileName().toString();
                var item = ItemStack.builder(SLOT_ICON).customName(Component.text(name, NamedTextColor.GREEN)).lore(lore).build();
                dataLayout.setItem(SLOTS[i], item, (player, i1, clickType, result) -> {
                    result.setCancel(true);
                    if (!(clickType == ClickType.LEFT_CLICK || clickType == ClickType.RIGHT_CLICK)) return;
                    creationConsumer.apply(clickType, entry);
                    player.setTag(TamiasSetup.MAP_PATH_TAG, entry.toString());
                    player.closeInventory();
                    EventDispatcher.call(new MapSelectionEvent(player, entry));
                });
            }

            return dataLayout;
        });
        invalidateDataLayout();
        setLayout(layout);
        register();
    }

    public void open(@NotNull Player player) {
        player.openInventory(getInventory());
    }
}
