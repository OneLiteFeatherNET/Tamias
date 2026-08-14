package net.theevilreaper.tamias.setup.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.inventory.layout.InventoryLayout;
import net.theevilreaper.aves.inventory.slot.ISlot;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.inventory.slot.PositionSlot;
import net.theevilreaper.tamias.setup.map.MapDataCategory;
import net.theevilreaper.tamias.setup.util.SetupItems;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;

/**
 * The {@link GameAreaDataInventory} is used to display the area data of a game map.
 * It shows the lower and upper corner of the game area.
 * The inventory is used during the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S3252")
public class GameAreaDataInventory extends PersonalInventoryBuilder {

    private static final int[] DATA_SLOTS = LayoutCalculator.from(12, 14);

    /**
     * Creates a new {@link GameAreaDataInventory} instance.
     *
     * @param mapBuilder the map builder to display
     */
    public GameAreaDataInventory(Player player, GameMapBuilder mapBuilder) {
        super(Component.text("Area data"), InventoryType.CHEST_3_ROW, player);
        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.blank(DATA_SLOTS);

            Vec lowerVec = mapBuilder.getAreaDataBuilder().lowerCorner();
            Vec upperVec = mapBuilder.getAreaDataBuilder().upperCorner();
            Pos lowerCorner = lowerVec == null ? null : new Pos(lowerVec.x(), lowerVec.y(), lowerVec.z());
            Pos upperCorner = upperVec == null ? null : new Pos(upperVec.x(), upperVec.y(), upperVec.z());

            ISlot areaLowerCornerSlot = new PositionSlot(MapDataCategory.AREA_LOWER_CORNER, lowerCorner);
            ISlot areaUpperCornerSlot = new PositionSlot(MapDataCategory.AREA_UPPER_CORNER, upperCorner);

            dataLayout.setItem(DATA_SLOTS[0], areaLowerCornerSlot);
            dataLayout.setItem(DATA_SLOTS[1], areaUpperCornerSlot);
            return dataLayout;
        });

        this.invalidateLayout();
        this.register();
    }
}
