package net.theevilreaper.tamias.setup.data;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Direction;
import net.minestom.server.world.DimensionType;
import net.onelitefeather.falco.anvil.FalcoAnvilLoader;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.setup.inventory.GameAreaDataInventory;
import net.theevilreaper.tamias.setup.inventory.GameMapDataInventory;
import net.theevilreaper.tamias.setup.inventory.GeneralMapDataInventory;
import net.theevilreaper.tamias.setup.map.MapDataCategory;

import java.util.Optional;
import java.util.UUID;

public class GameData extends InstanceSetupData {

    private final GeneralMapDataInventory inventory;
    private final GameMapDataInventory gameMapDataInventory;
    private final GameAreaDataInventory gameAreaDataInventory;
    private GameMapBuilder gameMapBuilder;

    /**
     * Constructs a new GameData instance.
     *
     * @param uuid       the UUID of the player
     * @param mapEntry   the map entry associated with this game data
     */
    public GameData(UUID uuid, MapEntry mapEntry) {
        super(uuid, mapEntry, BossBar.Color.RED);
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        this.loadData();
        if (player == null) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " is not online.");
        }

        this.inventory = new GeneralMapDataInventory(player, this.gameMapBuilder);
        this.gameMapDataInventory = new GameMapDataInventory(player, this.gameMapBuilder);
        this.gameAreaDataInventory = new GameAreaDataInventory(player, this.gameMapBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openInventory(InventoryTarget target) {
        switch (target) {
            case GENERAL -> this.inventory.open();
            case GAME -> this.gameMapDataInventory.open();
            case AREA -> this.gameAreaDataInventory.open();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerUpdate(InventoryTarget target) {
        switch (target) {
            case GENERAL -> this.inventory.invalidateDataLayout();
            case GAME -> this.gameMapDataInventory.invalidateDataLayout();
            case AREA -> this.gameAreaDataInventory.invalidateDataLayout();
        }
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
    public void setPosition(MapDataCategory category, Player player) {
        Pos pos = player.getPosition();
        switch (category) {
            case SPAWN -> {
                getMapBuilder().spawn(pos);
                triggerUpdate(InventoryTarget.GENERAL);
            }
            case SURVIVOR -> {
                this.gameMapBuilder.spawnLayerPos(pos);
                triggerUpdate(InventoryTarget.GAME);
            }
            case BOMBER_SPAWN -> {
                this.gameMapBuilder.bomberSpawn(pos);
                triggerUpdate(InventoryTarget.GAME);
            }
            case AREA_LOWER_CORNER -> {
                this.gameMapBuilder.areaLowerCorner(new Vec(pos.blockX(), pos.blockY(), pos.blockZ()));
                triggerUpdate(InventoryTarget.AREA);
            }
            case AREA_UPPER_CORNER -> {
                this.gameMapBuilder.areaUpperCorner(new Vec(pos.blockX(), pos.blockY(), pos.blockZ()));
                triggerUpdate(InventoryTarget.AREA);
            }
            default -> {}
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDirection(MapDataCategory category, Player player, Direction direction) {
        if (category == MapDataCategory.SURVIVOR_DIRECTION) {
            this.gameMapBuilder.spawnLayerDirection(direction);
            triggerUpdate(InventoryTarget.GAME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDataDelete(MapDataCategory category) {
        switch (category) {
            case SPAWN -> {
                gameMapBuilder.spawn(null);
                this.triggerUpdate(InventoryTarget.GENERAL);
            }
            case NAME -> {
                gameMapBuilder.name("Map");
                this.updateTitle();
                this.triggerUpdate(InventoryTarget.GENERAL);
            }
            case AUTHOR -> {
                gameMapBuilder.builders("");
                this.triggerUpdate(InventoryTarget.GENERAL);
            }
            case SURVIVOR -> {
                gameMapBuilder.spawnLayerPos(null);
                this.triggerUpdate(InventoryTarget.GAME);
            }
            case BOMBER_SPAWN -> {
                gameMapBuilder.bomberSpawn(null);
                this.triggerUpdate(InventoryTarget.GAME);
            }
            case AREA_LOWER_CORNER -> {
                gameMapBuilder.areaLowerCorner(null);
                this.triggerUpdate(InventoryTarget.AREA);
            }
            case AREA_UPPER_CORNER -> {
                gameMapBuilder.areaUpperCorner(null);
                this.triggerUpdate(InventoryTarget.AREA);
            }
            default -> throw new IllegalArgumentException("Unknown inventory category: " + category);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDataContextDelete(MapDataCategory category, Point point) {
        switch (category) {
            case SPAWN -> {
                if (samePosition(point, gameMapBuilder.getSpawn())) {
                    gameMapBuilder.spawn(null);
                    triggerUpdate(InventoryTarget.GENERAL);
                }
            }
            case SURVIVOR -> {
                if (samePosition(point, gameMapBuilder.getSpawnLayerBuilder().getPos())) {
                    gameMapBuilder.spawnLayerPos(null);
                    triggerUpdate(InventoryTarget.GAME);
                }
            }
            case BOMBER_SPAWN -> {
                if (samePosition(point, gameMapBuilder.getBomberInitialSpawn())) {
                    gameMapBuilder.bomberSpawn(null);
                    triggerUpdate(InventoryTarget.GAME);
                }
            }
            case AREA_LOWER_CORNER -> {
                if (samePosition(point, gameMapBuilder.getAreaDataBuilder().lowerCorner())) {
                    gameMapBuilder.areaLowerCorner(null);
                    triggerUpdate(InventoryTarget.AREA);
                }
            }
            case AREA_UPPER_CORNER -> {
                if (samePosition(point, gameMapBuilder.getAreaDataBuilder().upperCorner())) {
                    gameMapBuilder.areaUpperCorner(null);
                    triggerUpdate(InventoryTarget.AREA);
                }
            }
            default -> {}
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        if (!this.mapEntry.hasMapFile()) {
            this.mapEntry.createFile();
        }
        GsonUtil.FILE_HANDLER.save(mapEntry.getMapFile(), this.gameMapBuilder.build());
    }

    @Override
    public void teleport(Player player) {
        super.teleport(player);
        Pos spawnPoint = this.gameMapBuilder.getSpawn() == null
                ? SPAWN_POINT
                : this.gameMapBuilder.getSpawn();
        player.setInstance(this.instance, spawnPoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        this.inventory.unregister();
        this.gameMapDataInventory.unregister();
        this.gameAreaDataInventory.unregister();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData() {
        Optional<GameMap> mapData =
                this.mapEntry.hasMapFile()
                        ? GsonUtil.FILE_HANDLER.load(mapEntry.getMapFile(), GameMap.class)
                        : Optional.empty();

        this.gameMapBuilder = mapData
                .map(GameMapBuilder::new)
                .orElseGet(GameMapBuilder::new);

        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();

        this.chunkLoader = new FalcoAnvilLoader(this.mapEntry.getDirectoryRoot(), DimensionType.OVERWORLD.key());
        this.instance.setChunkLoader(this.chunkLoader);

        this.updateTitle();
        MinecraftServer.getInstanceManager().registerInstance(this.instance);
    }

    /**
     * Returns the GameMapBuilder instance used for building the game map.
     *
     * @return the builder instance
     */
    @Override
    public BaseMapBuilder getMapBuilder() {
        return this.gameMapBuilder;
    }
}
