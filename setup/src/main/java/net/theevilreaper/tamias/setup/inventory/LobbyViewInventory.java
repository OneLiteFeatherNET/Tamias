package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.entity.Player;
import net.theevilreaper.aves.inventory.GlobalInventoryBuilder;
import net.theevilreaper.aves.inventory.InventoryLayout;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.slot.ISlot;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.tamias.setup.inventory.slot.MultiStringSlot;
import net.theevilreaper.tamias.setup.inventory.slot.PositionSlot;
import net.theevilreaper.tamias.setup.inventory.slot.StringSlot;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import net.theevilreaper.tamias.setup.util.SetupItems;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;

/**
 * The {@link LobbyViewInventory} is used to display the data from a lobby map.
 * It shows the name, spawn and builders of the map.
 * The inventory is used during the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @see GlobalInventoryBuilder
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class LobbyViewInventory extends PersonalInventoryBuilder {

    private static final int[] DATA_SLOTS = LayoutCalculator.from(11, 13, 15);
    private final BaseMapBuilder mapBuilder;

    /**
     * Creates a new {@link LobbyViewInventory} instance.
     *
     * @param mapBuilder the map to display
     */
    public LobbyViewInventory(Player player, BaseMapBuilder mapBuilder) {
        super(Component.text("Map data"), InventoryType.CHEST_3_ROW, player);
        this.mapBuilder = mapBuilder;
        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.blank(DATA_SLOTS);
            ISlot mapNameSlot = new StringSlot(MapDataCategory.NAME, mapBuilder.getName());
            ISlot builderSlot = new MultiStringSlot(MapDataCategory.AUTHOR, mapBuilder.getBuilders());
            ISlot spawnSlot = new PositionSlot(MapDataCategory.SPAWN, mapBuilder.getSpawn());
            dataLayout.setItem(DATA_SLOTS[0], mapNameSlot);
            dataLayout.setItem(DATA_SLOTS[1], spawnSlot);
            dataLayout.setItem(DATA_SLOTS[2], builderSlot);
            return dataLayout;
        });

        this.invalidateLayout();
        this.invalidateDataLayout();
        this.register();
    }
}
