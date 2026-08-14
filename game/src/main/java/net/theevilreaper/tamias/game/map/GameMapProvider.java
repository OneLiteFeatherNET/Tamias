package net.theevilreaper.tamias.game.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.MapFilter;
import net.theevilreaper.tamias.common.map.provider.AbstractFalcoMapProvider;

import java.nio.file.Path;
import java.util.Optional;

/**
 * The {@link GameMapProvider} is responsible for the management of the instance which is required for the game.
 * It holds also a reference to the active map and provides some additional methods.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public final class GameMapProvider extends AbstractFalcoMapProvider implements MapFilter {

    private final SpawnArea spawnArea;
    private final SpawnPlacement spawnPlacement;
    private final GamePlacement gamePlacement;

    /**
     * Creates a new instance from the provider with the given parameters.
     *
     * @param path       the path to the map files
     * @param maxPlayers the maximum number of players, used to size the spawn area
     */
    public GameMapProvider(Path path,int maxPlayers) {
        super(GsonUtil.FILE_HANDLER, MapFilter::filterMapsForGame);
        this.loadMapEntries(path.resolve("game").resolve("maps"));
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        MapEntry map = this.mapEntries.getFirst();
        Optional<GameMap> loadedLobbyMap = fileHandler.load(map.getMapFile(), GameMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The map couldn't be loaded!");
        GameMap gameMap = loadedLobbyMap.get();
        this.activeMap = gameMap;
        this.activeInstance.setExplosionSupplier(new ExplosionCreator());
        this.registerFalcoInstance(this.activeInstance, map);
        if (this.activeMap.spawn() != null) {
            activeInstance.loadChunk(this.activeMap.spawn());
        }
        MinecraftServer.getInstanceManager().registerInstance(this.activeInstance);

        this.spawnArea = new SpawnArea(gameMap.getSpawnData(), maxPlayers);
        this.spawnPlacement = new SpawnPlacement(this.activeInstance, this.spawnArea);

        GameArea gameArea = new GameArea(gameMap.getGameAreaData());
        gameArea.calculatePositions();
        this.gamePlacement = new GamePlacement(this.activeInstance, gameArea);
    }

    /**
     * Loads the chunks of the active map to ensure that the players can spawn without issues.
     */
    public void loadGameChunks() {
        GameMap givenMap = (GameMap) this.activeMap;
        this.activeInstance.loadChunk(givenMap.spawn()).join();
    }

    /**
     * Teleports the player to the spawn of the active map.
     *
     * @param player      the player to teleport
     * @param instanceSet if the instance should be set for the player
     */
    @Override
    public void teleportToSpawn(Player player, boolean instanceSet) {
        if (instanceSet) {
            player.setInstance(this.activeInstance, this.activeMap.spawn());
            return;
        }
        player.teleport(this.activeMap.spawn());
    }

    /**
     * Saves the map to the given path.
     * This method is not supported in the game context, as maps are not saved during gameplay.
     *
     * @param path     the path where the map should be saved
     * @param baseMap  the map to save
     * @throws UnsupportedOperationException if called
     */
    @Override
    public void saveMap(Path path, BaseMap baseMap) {
        throw new UnsupportedOperationException("A game can't save a map");
    }

    /**
     * Returns the current active map.
     *
     * @return the active map
     */
    public BaseMap getActiveMap() {
        return this.activeMap;
    }

    /**
     * Returns the area used to spawn players during the lobby/build phase.
     *
     * @return the spawn area
     */
    public SpawnArea getSpawnArea() {
        return this.spawnArea;
    }

    /**
     * Returns the placement responsible for building/clearing the spawn area.
     *
     * @return the spawn placement
     */
    public SpawnPlacement getSpawnPlacement() {
        return this.spawnPlacement;
    }

    /**
     * Returns the placement responsible for building/clearing the game area.
     *
     * @return the game placement
     */
    public GamePlacement getGamePlacement() {
        return this.gamePlacement;
    }
}
