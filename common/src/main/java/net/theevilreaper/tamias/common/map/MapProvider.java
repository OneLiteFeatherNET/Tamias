package net.theevilreaper.tamias.common.map;

import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.BaseMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.GsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
        this.mapPath = originPath.resolve(GameConfig.MAP_FOLDER);
        this.mapPool = new MapPool(this.mapPath, filterMaps);
        this.fileHandler = new GsonFileHandler(GsonUtil.GSON);
    }

    /**
     * Tries to load the lobby map from the map folder.
     *
     * @param instanceContainer the instance container to bind the directory to it
     */
    private void loadLobbyMap(@NotNull InstanceContainer instanceContainer) {
        MapEntry mapEntry = this.mapPool.getMapEntry();
        Check.argCondition(!mapEntry.hasMapFile(), "The lobby map doesn't contain a map file!");
        Optional<BaseMap> loadedLobbyMap = fileHandler.load(mapEntry.getMapFile(), BaseMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The lobby map couldn't be loaded!");
        this.lobbyMap = loadedLobbyMap.get();

        instanceContainer.setChunkLoader(new AnvilLoader(mapEntry.path()));
        instanceContainer.enableAutoChunkLoad(true);

        if (this.lobbyMap.getSpawn() != null) {
            loadChunks(instanceContainer, this.lobbyMap.getSpawn());
        }
    }

    public void loadGameMap() {
        if (this.gameMapInstance != null) return;
        Check.argCondition(this.mapPool.getAvailableMaps().isEmpty(), "No maps available");
/*        Collections.shuffle(this.maps);
        if (this.maps.size() == 1) {
            path = this.maps.getFirst();
        } else {
            path = this.maps.get(ThreadLocalRandom.current().nextInt(this.maps.size()));
        }
        Check.argCondition(path == null, "Unable to load game map");
        var loader = new AnvilLoader(path);
        final var mapPath = path.resolve(MAP_FILE);
        Check.argCondition(!Files.exists(mapPath), "The game map doesn't contain a map file");
        var mapOptional = fileHandler.load(mapPath, GameMap.class);
        Check.argCondition(mapOptional.isEmpty(), "Something went wrong during map load");
        this.gameMap = mapOptional.get();
        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();
        MinecraftServer.getInstanceManager().registerInstance(container);
        container.setChunkLoader(loader);
        container.setExplosionSupplier(new ExplosionCreator());
        container.setTimeRate(0);
        this.spawnArea = new SpawnArea(container, this.gameMap.getInitialSurvivorSpawn(), Direction.valueOf(this.gameMap.getDirection().toUpperCase(Locale.ROOT)), 5);
        this.gameArea = new GameArea(container, this.gameMap.getLeftAreaPos(), this.gameMap.getRightAreaPos());
        this.gameMapInstance = container;
    */
    }

    public void loadGameChunks() {
        this.gameMapInstance.loadChunk(this.gameMap.getInitialSurvivorSpawn()).join();
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
}
