package net.theevilreaper.tamias.game.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.aves.map.provider.AbstractMapProvider;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.functional.LobbyMapPredicate;
import net.theevilreaper.tamias.common.util.MapFilter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;

public final class GameMapProvider extends AbstractMapProvider implements MapFilter {

    private final BaseMap lobbyMap;
    private final BaseMap gameMap;

    private InstanceContainer gameMapInstance;

    private SpawnArea spawnArea;
    private GameArea gameArea;

    public GameMapProvider(@NotNull Path path) {
        super(new GsonFileHandler(GsonUtil.GSON), MapFilter::filterMapsForGame);
        this.loadMapEntries(path.resolve("maps"));
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();

        LobbyMapPredicate predicate = new LobbyMapPredicate();
        MapEntry lobbyEntry = this.getEntries().stream().filter(predicate).findFirst().orElse(null);

        if (lobbyEntry == null) {
            throw new IllegalStateException("No lobby map found in the available maps");
        }

        Optional<BaseMap> loadedLobbyMap = fileHandler.load(lobbyEntry.getMapFile(), BaseMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The lobby map couldn't be loaded!");
        this.lobbyMap = loadedLobbyMap.get();
        this.registerInstance(this.activeInstance, lobbyEntry);
        if (this.lobbyMap.getSpawn() != null) {
            activeInstance.loadChunk(this.lobbyMap.getSpawn());
        }
        this.activeMap = this.lobbyMap;

        MapEntry gameEntry = this.getEntries().stream().filter(entry -> !predicate.test(entry))
                        .skip(new Random().nextInt(0, this.getEntries().size() - 1)).findFirst().orElse(null);

        Check.argCondition(gameEntry == null, "No game map found in the available maps");
        Check.argCondition(!gameEntry.hasMapFile(), "The game map doesn't contain a map file!");
        Optional<GameMap> loadedGameMap = fileHandler.load(gameEntry.getMapFile(), GameMap.class);
        Check.argCondition(loadedGameMap.isEmpty(), "The game map couldn't be loaded!");
        this.gameMap = loadedGameMap.get();
        //((GameMap) this.gameMap).setMapEntry(gameEntry);

        MinecraftServer.getInstanceManager().registerInstance(this.activeInstance);
        this.createGameMapContainer();
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
      //  this.spawnArea.teleport(this.gameMapInstance, players, switchInstance);
    }

    public void resetSpawnArea() {
        this.spawnArea.reset();
    }

    public void resetGameArea() {
        this.gameArea.reset();
    }

    public void triggerSpawnPlacement() {

        //this.spawnArea.triggerPlacement();
    }

    public void cleanupMapPlacement() {
      //  this.gameArea.getTask().cancel();
    }

    public @NotNull Task triggerGamePlacement() {
        return null;
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
