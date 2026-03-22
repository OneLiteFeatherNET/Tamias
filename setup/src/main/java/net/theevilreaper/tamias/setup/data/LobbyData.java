package net.theevilreaper.tamias.setup.data;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.setup.inventory.LobbyViewInventory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

import static net.theevilreaper.tamias.setup.map.SetupMapProvider.FILE_HANDLER;

public final class LobbyData extends InstanceSetupData {

    private LobbyViewInventory viewInventory;
    private BaseMapBuilder mapBuilder;

    public LobbyData(@NotNull UUID uuid, @NotNull MapEntry mapEntry) {
        super(uuid, mapEntry, BossBar.Color.GREEN);
        this.loadData();
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);

        if (player == null) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " is not online.");
        }

        this.viewInventory = null;
    }

    @Override
    public void openInventory(@NotNull Player player) {
        player.openInventory(this.viewInventory.getInventory());
    }

    @Override
    public void triggerUpdate() {
        this.viewInventory.invalidateDataLayout();
    }

    @Override
    public void save() {
        if (!Files.exists(mapEntry.getMapFile())) {
            this.mapEntry.createFile();
        }
        FILE_HANDLER.save(mapEntry.getMapFile(), BaseMap.class);
    }

    @Override
    public void teleport(@NotNull Player player) {
        super.teleport(player);
        Pos spawnPoint = this.mapBuilder.getSpawn() == null
                ? SPAWN_POINT
                : this.mapBuilder.getSpawn();
        player.setInstance(this.instance, spawnPoint);
    }

    @Override
    public void reset() {
        super.reset();
        this.viewInventory.unregister();
    }

    @Override
    public void loadData() {
        if (this.mapEntry == null) {
            this.mapBuilder = BaseMap.builder();
        } else {
            Optional<BaseMap> mapData = FILE_HANDLER.load(mapEntry.getMapFile(), BaseMap.class);

            mapData.ifPresentOrElse(baseMap -> {
                this.mapBuilder = BaseMap.builder(baseMap);
            }, () -> this.mapBuilder = BaseMap.builder());
        }

        this.viewInventory = new LobbyViewInventory(this.mapBuilder);

        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        AnvilLoader anvilLoader = new AnvilLoader(this.mapEntry.getDirectoryRoot());
        this.instance.setChunkLoader(anvilLoader);
        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
    }

    @Override
    public BaseMapBuilder getMapBuilder() {
        return mapBuilder;
    }
}
