package net.theevilreaper.tamias.setup.data.v2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.theevilreaper.aves.file.FileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

public final class LobbyData extends InstanceSetupData<BaseMap> {

    private final FileHandler fileHandler;
    private final LobbyViewInventory viewInventory;

    public LobbyData(@NotNull UUID uuid, @NotNull MapEntry mapEntry, @NotNull FileHandler fileHandler) {
        super(uuid, mapEntry);
        this.fileHandler = fileHandler;
        this.loadData();
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);

        if (player == null) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " is not online.");
        }

        this.viewInventory = new LobbyViewInventory(this.map);
    }

    public void openInventory(@NotNull Player player) {
        player.openInventory(this.viewInventory.getInventory());
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
        this.viewInventory.unregister();
    }

    @Override
    public void loadData() {
        if (this.mapEntry != null) return;
        Optional<BaseMap> mapData = fileHandler.load(mapEntry.getMapFile(), BaseMap.class);
        // Initialize with a new BaseMap if loading fails
        this.map = mapData.orElseGet(BaseMap::new);

        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        AnvilLoader anvilLoader = new AnvilLoader(this.mapEntry.getDirectoryRoot());
        this.instance.setChunkLoader(anvilLoader);
        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
    }
}
