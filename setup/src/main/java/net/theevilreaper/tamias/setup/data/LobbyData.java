package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import net.theevilreaper.tamias.setup.state.SetupState;
import org.jetbrains.annotations.NotNull;

public final class LobbyData extends SetupDataImpl {

    private final LobbyViewInventory inventory;

    LobbyData(@NotNull Player player, @NotNull MapEntry mapEntry, @NotNull SetupState mode, @NotNull BaseMap baseMap) {
        super(player, mapEntry, mode, baseMap);
        this.inventory = new LobbyViewInventory(baseMap);
    }

    @Override
    public void openInventory() {
        player.openInventory(inventory.getInventory());
    }

    @Override
    public void triggerInventoryUpdate() {
        inventory.invalidateDataLayout();
    }

    @Override
    public void reset() {
        super.reset();
        this.inventory.unregister();
    }
}
