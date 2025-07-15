package net.theevilreaper.tamias.setup.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.utils.Direction;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

public class GameData extends InstanceSetupData<GameMap> {

    private final FileHandler fileHandler;
    private final LobbyViewInventory inventory;
   // private GameArea areaDataBuilder;
    private boolean areaMode;

    public GameData(@NotNull UUID uuid, @NotNull MapEntry mapEntry, @NotNull FileHandler fileHandler) {
        super(uuid, mapEntry);
        this.fileHandler = fileHandler;
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);

        if (player == null) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " is not online.");
        }

        this.inventory = new LobbyViewInventory(this.map);
    }

    public void swapAreaMode() {
        this.areaMode = !this.areaMode;

        if (this.areaMode) {
          //  this.areaDataBuilder = GameAreaData.builder();
            return;
        }
        GameMap gameMap = (GameMap) this.map;
      //  gameMap.setGameAreaData(this.areaDataBuilder.build());
    }

    @Override
    public void openInventory(@NotNull Player player) {
        player.openInventory(this.inventory.getInventory());
    }

    @Override
    public void triggerUpdate() {
        this.inventory.invalidateDataLayout();
    }

    /**
     * Sets the left corner of the area.
     *
     * @param vec the left corner
     */
    public void setLeftCorner(@NotNull Vec vec) {
    //    this.areaDataBuilder.lowerCorner(vec);
    }

    /**
     * Sets the direction of the area.
     *
     * @param direction the direction of the area
     */
    public void setDirection(@NotNull Direction direction) {
    //    this.areaDataBuilder.facing(direction);
    }

    /**
     * Sets the right corner of the area.
     *
     * @param vec the right corner
     */
    public void setRightCorner(@NotNull Vec vec) {
        // this.areaDataBuilder.upperCorner(vec);
    }

    @Override
    public void save() {
        if (!Files.exists(mapEntry.getMapFile())) {
            this.mapEntry.createFile();
        }
        this.fileHandler.save(mapEntry.getMapFile(), BaseMap.class);
    }

    @Override
    public void reset() {
        super.reset();
        this.inventory.unregister();
    }

    @Override
    public void loadData() {
        if (this.mapEntry != null) return;
        Optional<GameMap> mapData = fileHandler.load(mapEntry.getMapFile(), GameMap.class);
        // Initialize with a new BaseMap if loading fails
        this.map = mapData.orElseGet(GameMap::new);

        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        AnvilLoader anvilLoader = new AnvilLoader(this.mapEntry.getDirectoryRoot());
        this.instance.setChunkLoader(anvilLoader);
        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
    }

    public boolean hasAreaMode() {
        return areaMode;
    }
}
