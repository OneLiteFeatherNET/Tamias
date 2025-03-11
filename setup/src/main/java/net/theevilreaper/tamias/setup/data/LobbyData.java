package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import org.jetbrains.annotations.NotNull;

public final class LobbyData extends SetupDataImpl {

    private final LobbyViewInventory inventory;

    LobbyData(@NotNull Player player, @NotNull MapEntry mapEntry, @NotNull BaseMap baseMap) {
        super(player, mapEntry, baseMap);
        this.inventory = new LobbyViewInventory(baseMap);

        this.title = Component.text("Setup mode: ", NamedTextColor.GRAY)
                .append(Component.text("Lobby", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(", Map: ", NamedTextColor.GRAY))
                .append(Component.text(mapEntry.getDirectoryRoot().getFileName().toString(), NamedTextColor.LIGHT_PURPLE));
    }

    @Override
    public void swapAreaMode() {
        throw new UnsupportedOperationException("A LobbyData can't swap the area mode");
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

    @Override
    public boolean hasAreaMode() {
        return false;
    }
}
