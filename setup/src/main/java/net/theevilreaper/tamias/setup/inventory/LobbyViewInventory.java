package net.theevilreaper.tamias.setup.inventory;

import net.minestom.server.coordinate.Point;
import net.theevilreaper.aves.inventory.GlobalInventoryBuilder;
import net.theevilreaper.aves.inventory.InventoryLayout;
import net.theevilreaper.aves.inventory.util.LayoutCalculator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.tamias.setup.inventory.slot.EmptyItemSlot;
import net.theevilreaper.tamias.setup.inventory.slot.MultipleStringItemSlot;
import net.theevilreaper.tamias.setup.inventory.slot.SpawnItemSlot;
import net.theevilreaper.tamias.setup.inventory.slot.StringItemSlot;
import net.theevilreaper.tamias.setup.util.SetupItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.theevilreaper.aves.inventory.util.InventoryConstants.CANCEL_CLICK;
import static net.theevilreaper.tamias.setup.dialog.event.PlayerDialogRequestEvent.*;

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
public class LobbyViewInventory extends GlobalInventoryBuilder {

    private static final int[] DATA_SLOTS = LayoutCalculator.from(11, 13, 15);
    private final ConfirmInventory confirmInventory;
    private final BaseMapBuilder mapBuilder;

    /**
     * Creates a new {@link LobbyViewInventory} instance.
     *
     * @param mapBuilder the map to display
     */
    public LobbyViewInventory(BaseMapBuilder mapBuilder) {
        super(Component.text("Lobby data"), InventoryType.CHEST_3_ROW);
        this.mapBuilder = mapBuilder;
        this.confirmInventory = new ConfirmInventory(this::handleConfirmClick);
        InventoryLayout layout = InventoryLayout.fromType(getType());
        layout.setItems(LayoutCalculator.quad(0, getType().getSize() - 1), SetupItems.DECORATION, CANCEL_CLICK);
        this.setLayout(layout);

        this.setDataLayoutFunction(dataLayout -> {
            dataLayout = dataLayout == null ? InventoryLayout.fromType(getType()) : dataLayout;
            dataLayout.blank(DATA_SLOTS);
            this.setMapNameItem(dataLayout, mapBuilder.getName());
            this.setSpawnItem(dataLayout, mapBuilder.getSpawn());
            this.setAuthorItem(dataLayout, mapBuilder.getBuilders());
            return dataLayout;
        });

        this.invalidateLayout();
        this.invalidateDataLayout();
        this.register();
    }

    /**
     * Sets the map name item.
     *
     * @param dataLayout the layout which should receive the item
     * @param name       the name of the map
     */
    private void setMapNameItem(InventoryLayout dataLayout, @Nullable String name) {
        if (name == null) {
            System.out.println("Name is null");
            dataLayout.setItem(DATA_SLOTS[0], new EmptyItemSlot(Target.SETUP_NAME));
            return;
        }
        dataLayout.setItem(DATA_SLOTS[0], new StringItemSlot(Component.text("Name", NamedTextColor.GOLD), name));
    }

    /**
     * Sets the spawn item.
     *
     * @param layout the layout which should receive the item
     * @param spawn  the spawn point
     */
    private void setSpawnItem(InventoryLayout layout, @Nullable Point spawn) {
        if (spawn == null) {
            layout.setItem(DATA_SLOTS[1], new EmptyItemSlot(Target.SETUP_BLOCK_BOUNCE));
        } else {
            layout.setItem(DATA_SLOTS[1], SpawnItemSlot.asSpawn(spawn, this::openConfirmInventory));
        }
    }

    /**
     * Sets the author item.
     *
     * @param layout the layout which should receive the item
     * @param author the author of the map
     */
    private void setAuthorItem(InventoryLayout layout, @Nullable List<String> author) {
        if (author == null || author.isEmpty()) {
            layout.setItem(DATA_SLOTS[2], new EmptyItemSlot(Target.SETUP_REQUEST_AUTHOR));
        } else {
            layout.setItem(DATA_SLOTS[2], new MultipleStringItemSlot(Component.text("Author", NamedTextColor.GOLD), author));
        }
    }

    /**
     * Opens the confirmation inventory.
     *
     * @param player the player to open the inventory
     */
    private void openConfirmInventory(Player player) {
        player.closeInventory();
        confirmInventory.register();
        player.openInventory(confirmInventory.getInventory());
    }

    /**
     * Handles the confirmation logic.
     *
     * @param player the player who clicked
     */
    private void handleConfirmClick(Player player) {
        player.closeInventory();
        mapBuilder.spawn(null);
        invalidateDataLayout();
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> player.openInventory(this.getInventory()));
    }
}
