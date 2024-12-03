package net.theevilreaper.tamias.common.map;

import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;

public final class MapProvider {

    private final Path mapPath;
    private final FileHandler fileHandler;
    private final MapPool mapPool;
    private BaseMap lobbyMap;
    private GameMap gameMap;
    private InstanceContainer gameMapInstance;
    private SpawnArea spawnArea;
    private GameArea gameArea;

    public MapProvider(@NotNull Path originPath, @NotNull Function<Stream<Path>, List<MapEntry>> filterMaps) {
        this.mapPath = originPath;
        this.mapPool = new MapPool(this.mapPath, filterMaps);
        this.fileHandler = new GsonFileHandler(GsonUtil.GSON);
    }

    public void saveMap(@NotNull Path path, @NotNull BaseMap baseMap) {
        this.fileHandler.save(path, baseMap instanceof GameMap gameMap ? gameMap : baseMap);
    }

    /**
     * Tries to load the lobby map from the map folder.
     *
     * @param instanceContainer the instance container to bind the directory to it
     */
    public void loadLobbyMap(@NotNull InstanceContainer instanceContainer) {
        MapEntry mapEntry = this.mapPool.getMapEntry();
        Check.argCondition(!mapEntry.hasMapFile(), "The lobby map doesn't contain a map file!");
        Optional<BaseMap> loadedLobbyMap = fileHandler.load(mapEntry.getMapFile(), BaseMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The lobby map couldn't be loaded!");
        this.lobbyMap = loadedLobbyMap.get();

        instanceContainer.setChunkLoader(new AnvilLoader(mapEntry.getDirectoryRoot()));
        instanceContainer.enableAutoChunkLoad(true);

        if (this.lobbyMap.getSpawn() != null) {
            loadChunks(instanceContainer, this.lobbyMap.getSpawn());
        }
    }

    public void loadGameMap() {
        if (this.gameMapInstance != null) return;
        Check.argCondition(this.mapPool.getAvailableMaps().isEmpty(), "No maps available");
        List<MapEntry> maps = new ArrayList<>(this.mapPool.getAvailableMaps());
        Collections.shuffle(maps);
        MapEntry mapEntry;
        if (maps.size() == 1) {
            mapEntry = maps.getFirst();
        } else {
            mapEntry = maps.get(ThreadLocalRandom.current().nextInt(maps.size()));
        }

        Check.argCondition(!mapEntry.hasMapFile(), "The game map doesn't contain a map file");
        AnvilLoader anvilLoader = new AnvilLoader(mapEntry.getDirectoryRoot());
        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer(anvilLoader);
        container.setExplosionSupplier(new ExplosionCreator());
        container.enableAutoChunkLoad(true);
        container.setTimeRate(0);

        Optional<GameMap> loadedGameMap = fileHandler.load(mapEntry.getMapFile(), GameMap.class);
        this.gameMap = loadedGameMap.get();
        this.spawnArea = new SpawnArea(container, this.gameMap.getSpawnData(), 16);
        this.gameArea = new GameArea(container, this.gameMap.getGameAreaData());
        this.gameMapInstance = container;
    }

    public void loadGameChunks() {
        this.gameMapInstance.loadChunk(this.gameMap.getSpawnData().pos()).join();
    }

    public void teleportPlayers(@NotNull List<Player> players) {
        this.spawnArea.teleport(this.gameMapInstance, players);
    }

    public @Nullable BaseMap getActiveMap() {
        if (lobbyMap != null) return lobbyMap;
        if (gameMap != null) return gameMap;
        return null;
    }

    public <T extends Point> void loadChunks(@NotNull Instance instance, @NotNull T @NotNull ... positions) {
        if (positions.length == 0) return;
        for (int i = 0; i < positions.length; i++) {
            final var pos = positions[i];
            if (!instance.isChunkLoaded(pos)) {
                instance.loadChunk(pos);
            }
        }
    }

    public @Nullable SpawnArea getSpawnArea() {
        return spawnArea;
    }

    public @Nullable GameArea getGameArea() {
        return gameArea;
    }

    public @NotNull List<MapEntry> getMapEntries() {
        return this.mapPool.getAvailableMaps();
    }
}
