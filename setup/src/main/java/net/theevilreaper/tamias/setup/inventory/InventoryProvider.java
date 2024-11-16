package net.theevilreaper.tamias.setup.inventory;

import de.icevizion.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public final class InventoryProvider {

    private final MapSetupInventory mapSetupInventory;

    public InventoryProvider(@NotNull Supplier<List<MapEntry>> maps) {
        this.mapSetupInventory = new MapSetupInventory(maps);
    }

    public void openMapSetupInventory(@NotNull Player player) {
        mapSetupInventory.open(player);
    }
}
