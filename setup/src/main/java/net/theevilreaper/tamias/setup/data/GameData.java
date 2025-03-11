package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import org.jetbrains.annotations.NotNull;

public final class GameData extends SetupDataImpl {

    private GameAreaData.Builder areaDataBuilder;
    private final LobbyViewInventory inventory;

    private boolean areaMode;

    GameData(@NotNull Player player, @NotNull MapEntry mapEntry, @NotNull BaseMap baseMap) {
        super(player, mapEntry, baseMap);
        this.inventory = new LobbyViewInventory(baseMap);
        this.title = Component.text("Setup mode: ", NamedTextColor.GRAY)
                .append(Component.text("Game", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(", Map: ", NamedTextColor.GRAY))
                .append(Component.text(mapEntry.getDirectoryRoot().getFileName().toString(), NamedTextColor.LIGHT_PURPLE));
    }

    @Override
    public void swapAreaMode() {
        this.areaMode = !this.areaMode;

        if (this.areaMode) {
            this.areaDataBuilder = GameAreaData.builder();
        } else {
            GameMap gameMap = (GameMap) this.baseMap;
            gameMap.setGameAreaData(this.areaDataBuilder.build());
        }
    }

    public void setLeftCorner(@NotNull Vec vec) {
        this.areaDataBuilder.lowerCorner(vec);
    }

    public void setDirection(@NotNull Direction direction) {
        this.areaDataBuilder.facing(direction);
    }

    public void setRightCorner(@NotNull Vec vec) {
        this.areaDataBuilder.upperCorner(vec);
    }

    @Override
    public void openInventory() {
        player.openInventory(inventory.getInventory());
    }

    @Override
    public void triggerInventoryUpdate() {
        this.inventory.invalidateDataLayout();
    }

    @Override
    public void reset() {
        super.reset();
        this.inventory.unregister();
    }

    @Override
    public boolean hasAreaMode() {
        return this.areaMode;
    }
}
