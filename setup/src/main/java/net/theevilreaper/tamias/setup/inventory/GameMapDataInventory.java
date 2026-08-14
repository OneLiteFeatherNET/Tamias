package net.theevilreaper.tamias.setup.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.slot.ISlot;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.inventory.slot.DirectionSlot;
import net.theevilreaper.tamias.setup.inventory.slot.PositionSlot;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import net.theevilreaper.tamias.setup.util.SetupItems;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;

/**
 * The {@link GameMapDataInventory} is used to display the game specific data from a map.
 * It shows the bomber spawn and the survivor spawn layer of the map.
 * The inventory is used during the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class GameMapDataInventory extends PersonalInventoryBuilder {

    private static final int[] DATA_SLOTS = LayoutCalculator.from(11, 13, 15);

    /**
     * Creates a new {@link GameMapDataInventory} instance.
     *
     * @param mapBuilder the map builder to display
     */
    public GameMapDataInventory(Player player, GameMapBuilder mapBuilder) {
        super(Component.text("Game data"), InventoryType.CHEST_3_ROW, player);
        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.blank(DATA_SLOTS);

            ISlot bomberSpawnSlot = new PositionSlot(MapDataCategory.BOMBER_SPAWN, mapBuilder.getBomberInitialSpawn());
            ISlot survivorSlot = new PositionSlot(MapDataCategory.SURVIVOR, mapBuilder.getSpawnLayerBuilder().getPos());
            ISlot survivorDirectionSlot = new DirectionSlot(MapDataCategory.SURVIVOR_DIRECTION, mapBuilder.getSpawnLayerBuilder().getDirection());

            dataLayout.setItem(DATA_SLOTS[0], bomberSpawnSlot);
            dataLayout.setItem(DATA_SLOTS[1], survivorSlot);
            dataLayout.setItem(DATA_SLOTS[2], survivorDirectionSlot);
            return dataLayout;
        });

        this.invalidateLayout();
        this.register();
    }
}
