package net.theevilreaper.tamias.game.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.aves.map.provider.AbstractMapProvider;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.area.holder.Placement;
import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.MapFilter;
import org.jetbrains.annotations.NotNull;

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
public final class GameMapProvider extends AbstractMapProvider implements MapFilter {

    private final Placement spawnPlacement;
    private final Placement gamePlacement;

    /**
     * Creates a new instance from the provider with the given parameters.
     *
     * @param path the path to the map files
     */
    public GameMapProvider(@NotNull Path path, int maxPlayers) {
        super(new GsonFileHandler(GsonUtil.GSON), MapFilter::filterMapsForGame);
        this.mapEntries = this.loadMapEntries(path.resolve("maps"));
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();

        MapEntry map = this.mapEntries.getFirst();
        Optional<GameMap> loadedLobbyMap = fileHandler.load(map.getMapFile(), GameMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The map couldn't be loaded!");

        GameMap gameMap = loadedLobbyMap.get();
        this.activeMap = gameMap;
        this.activeInstance.setExplosionSupplier(new ExplosionCreator());
        this.registerInstance(this.activeInstance, map);
        if (this.activeMap.getSpawn() != null) {
            activeInstance.loadChunk(this.activeMap.getSpawn());
        }
        MinecraftServer.getInstanceManager().registerInstance(this.activeInstance);

        this.spawnPlacement = new SpawnPlacement(this.activeInstance, new SpawnArea(gameMap.getSpawnData(), maxPlayers));
        this.gamePlacement = new GamePlacement(this.activeInstance, new GameArea(gameMap.getGameAreaData()));
    }

    /**
     * Loads the chunks of the active map to ensure that the players can spawn without issues.
     */
    public void loadGameChunks() {
        GameMap givenMap = (GameMap) this.activeMap;
        if (givenMap.getSpawn() == null) return;
        this.activeInstance.loadChunk(givenMap.getSpawn()).join();
    }

    /**
     * Teleports the player to the spawn of the active map.
     *
     * @param player      the player to teleport
     * @param instanceSet if the instance should be set for the player
     */
    @Override
    public void teleportToSpawn(@NotNull Player player, boolean instanceSet) {
        if (instanceSet) {
            player.setInstance(this.activeInstance, this.activeMap.getSpawn());
            return;
        }
        player.teleport(this.activeMap.getSpawn());
    }

    /**
     * Saves the map to the given path.
     * This method is not supported in the game context, as maps are not saved during gameplay.
     *
     * @param path    the path where the map should be saved
     * @param baseMap the map to save
     * @throws UnsupportedOperationException if called
     */
    @Override
    public void saveMap(@NotNull Path path, @NotNull BaseMap baseMap) {
        throw new UnsupportedOperationException("A game can't save a map");
    }

    /**
     * Returns the current active map.
     *
     * @return the active map
     */
    public @NotNull BaseMap getActiveMap() {
        return this.activeMap;
    }

    /**
     * Returns the placement for the spawn area.
     *
     * @return the spawn area placement
     */
    public @NotNull Placement getSpawnPlacement() {
        return this.spawnPlacement;
    }

    /**
     * Returns the placement for the game area.
     *
     * @return the game area placement
     */
    public @NotNull Placement getGamePlacement() {
        return this.gamePlacement;
    }
}
