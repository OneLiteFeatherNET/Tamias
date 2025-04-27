package net.theevilreaper.tamias.game.map;

import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.MapPool;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.common.util.MapFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class GameMapProvider implements MapProvider, MapFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMapProvider.class);
    private final FileHandler fileHandler;
    private final MapPool mapPool;
    private final BaseMap lobbyMap;
    private final BaseMap gameMap;

    private InstanceContainer activeInstance;
    private BaseMap activeMap;

    private InstanceContainer gameMapInstance;

    private SpawnArea spawnArea;
    private GameArea gameArea;

    public GameMapProvider() {
        this.mapPool = new MapPool(ROOT_FOLDER.resolve(GameConfig.MAP_FOLDER), this::filterMapsForGame);
        this.fileHandler = new GsonFileHandler(GsonUtil.GSON);
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();

        MapEntry lobbyEntry = this.mapPool.getMapEntry();
        Check.argCondition(!lobbyEntry.hasMapFile(), "The lobby map doesn't contain a map file!");
        Optional<BaseMap> loadedLobbyMap = fileHandler.load(lobbyEntry.getMapFile(), BaseMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The lobby map couldn't be loaded!");
        this.lobbyMap = loadedLobbyMap.get();
        this.loadBaseMap(lobbyEntry);

        MapEntry gameEntry = this.mapPool.getAvailableMaps().getFirst();

        Check.argCondition(!gameEntry.hasMapFile(), "The game map doesn't contain a map file!");
        Optional<GameMap> loadedGameMap = fileHandler.load(gameEntry.getMapFile(), GameMap.class);
        Check.argCondition(loadedGameMap.isEmpty(), "The game map couldn't be loaded!");
        this.gameMap = loadedGameMap.get();
        //((GameMap) this.gameMap).setMapEntry(gameEntry);
        this.gameArea = new GameArea(null, ((GameMap) this.gameMap).getGameAreaData());
        this.spawnArea = new SpawnArea(null, ((GameMap) this.gameMap).getSpawnData(), 16);

        MinecraftServer.getInstanceManager().registerInstance(this.activeInstance);
        this.createGameMapContainer();
    }

    private void loadBaseMap(@NotNull MapEntry mapEntry) {
        activeInstance.setChunkLoader(new AnvilLoader(mapEntry.getDirectoryRoot()));
        activeInstance.enableAutoChunkLoad(true);
        if (this.lobbyMap.getSpawn() != null) {
          //  loadChunks(activeInstance, this.lobbyMap.getSpawn());
        }
        this.activeMap = this.lobbyMap;
    }

    public void createGameMapContainer() {
        if (this.gameMapInstance != null) return;
        this.gameMapInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.gameMapInstance.setExplosionSupplier(new ExplosionCreator());
        this.gameMapInstance.enableAutoChunkLoad(true);
        this.gameMapInstance.setTimeRate(0);
        GameMap castetMap = ((GameMap) this.gameMap);
        //MapEntry mapEntry = castetMap.getMapEntry();
       // Check.argCondition(mapEntry == null, "The game map contains no map entry reference");
       // AnvilLoader anvilLoader = new AnvilLoader(mapEntry.getDirectoryRoot());
      //  this.gameMapInstance.setChunkLoader();

        //this.gameArea.excludeBlocks(this.gameMapInstance);
    }

    public void switchInstanceHolding() {
        this.activeMap = this.gameMap;
        MinecraftServer.getInstanceManager().registerInstance(this.gameMapInstance);
        this.activeInstance = this.gameMapInstance;
        //this.gameArea.setInstanceSupplier(() -> this.gameMapInstance);
    }

    public void teleportPlayers(@NotNull List<Player> players, BooleanSupplier switchInstance) {
        this.spawnArea.teleport(this.gameMapInstance, players, switchInstance);
    }

    public void resetSpawnArea() {
        this.spawnArea.reset();
    }

    public void resetGameArea() {
        this.gameArea.reset();
    }

    public void triggerSpawnPlacement() {
        this.spawnArea.triggerPlacement();
    }

    public void cleanupMapPlacement() {
        this.gameArea.getTask().cancel();
    }

    public @NotNull Task triggerGamePlacement() {
        LOGGER.warn("Triggering game placement");
        this.gameArea.triggerPlacement();
        return this.gameArea.getTask();
    }

    public void loadGameChunks() {
        GameMap givenMap = (GameMap) this.gameMap;
        this.gameMapInstance.loadChunk(givenMap.getSpawnData().pos()).join();
    }

    @Override
    public void teleportToSpawn(@NotNull Player player, boolean instanceSet) {
        if (this.activeMap instanceof GameMap) return;
        player.teleport(this.activeMap.getSpawn());
    }

    @Override
    public void saveMap(@NotNull Path path, @NotNull BaseMap baseMap) {
        throw new UnsupportedOperationException("A game can't save a map");
    }

    @Override
    public @UnmodifiableView @NotNull List<MapEntry> getEntries() {
        return this.mapPool.getAvailableMaps();
    }

    @Override
    public @NotNull Supplier<@Nullable Instance> getActiveInstance() {
        return () -> this.activeInstance;
    }

    public @NotNull SpawnArea getSpawnArea() {
        return this.spawnArea;
    }

    public @NotNull GameArea getGameArea() {
        return this.gameArea;
    }

    public @NotNull BaseMap getActiveMap() {
        return this.activeMap;
    }
}
