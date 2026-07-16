package net.theevilreaper.tamias.setup.data;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.world.DimensionType;
import net.theevilreaper.aves.inventory.PersonalInventoryBuilder;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.setup.inventory.GeneralMapDataInventory;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

import java.util.Optional;
import java.util.UUID;

public final class LobbyData extends InstanceSetupData {

    private final PersonalInventoryBuilder viewInventory;
    private BaseMapBuilder mapBuilder;

    public LobbyData(UUID uuid, MapEntry mapEntry) {
        super(uuid, mapEntry, BossBar.Color.GREEN);
        this.loadData();
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);

        if (player == null) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " is not online.");
        }

        this.viewInventory = new GeneralMapDataInventory(player, this.mapBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(MapDataCategory category, Player player) {
        if (category == MapDataCategory.SPAWN) {
            getMapBuilder().spawn(player.getPosition());
            triggerUpdate(InventoryTarget.GENERAL);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openInventory(InventoryTarget target) {
        this.viewInventory.open();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerUpdate(InventoryTarget target) {
        this.viewInventory.invalidateDataLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTitle() {
        if (getMapBuilder().getName().equalsIgnoreCase("Map")) {
            this.title = null;
            super.updateTitle();
            return;
        }
        this.title = Component.text("Map: ").append(Component.text(getMapBuilder().getName(), MapDataCategory.NAME.getColor()));
        super.updateTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDataDelete(MapDataCategory category) {
        switch (category) {
            case SPAWN -> mapBuilder.spawn(null);
            case NAME -> {
                mapBuilder.name("Map");
                this.updateTitle();
            }
            case AUTHOR -> mapBuilder.builders("");
            default -> throw new IllegalArgumentException("Unknown inventory category: " + category);
        }
        this.triggerUpdate(InventoryTarget.GENERAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        if (!mapEntry.hasMapFile()) {
            this.mapEntry.createFile();
        }
        GsonUtil.FILE_HANDLER.save(mapEntry.getMapFile(), this.mapBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teleport(Player player) {
        super.teleport(player);
        Pos spawnPoint = this.mapBuilder.getSpawn() == null
                ? SPAWN_POINT
                : this.mapBuilder.getSpawn();
        player.setInstance(this.instance, spawnPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        this.viewInventory.unregister();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData() {
        Optional<BaseMap> mapData =
                this.mapEntry.hasMapFile()
                        ? GsonUtil.FILE_HANDLER.load(mapEntry.getMapFile(), BaseMap.class)
                        : Optional.empty();

        this.mapBuilder = mapData
                .map(BaseMap::builder)
                .orElseGet(BaseMap::builder);

        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();

        AnvilLoader anvilLoader = new AnvilLoader(this.mapEntry.getDirectoryRoot(), DimensionType.OVERWORLD.key());
        this.instance.setChunkLoader(anvilLoader);

        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
    }

    @Override
    public BaseMapBuilder getMapBuilder() {
        return mapBuilder;
    }
}
