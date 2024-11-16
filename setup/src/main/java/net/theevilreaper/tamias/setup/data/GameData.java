package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Direction;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.layer.GameAreaData;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import net.theevilreaper.tamias.setup.state.SetupState;
import org.jetbrains.annotations.NotNull;

public final class GameData extends SetupDataImpl {

    private GameAreaData.Builder areaDataBuilder;
    private final LobbyViewInventory inventory;

    GameData(@NotNull Player player, @NotNull MapEntry mapEntry, @NotNull SetupState mode, @NotNull BaseMap baseMap) {
        super(player, mapEntry, mode, baseMap);
        this.inventory = new LobbyViewInventory(baseMap);
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
}
