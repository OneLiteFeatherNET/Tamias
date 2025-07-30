package net.theevilreaper.tamias.game.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.aves.map.provider.AbstractMapProvider;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.MapFilter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

public final class GameMapProvider extends AbstractMapProvider implements MapFilter {

    /**
     * Creates a new instance from the provider with teh given parameters.
     * @param path the path to the map files
     */
    public GameMapProvider(@NotNull Path path) {
        super(new GsonFileHandler(GsonUtil.GSON), MapFilter::filterMapsForGame);
        this.mapEntries = this.loadMapEntries(path.resolve("maps"));
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();

        MapEntry map = this.mapEntries.getFirst();
        Optional<GameMap> loadedLobbyMap = fileHandler.load(map.getMapFile(), GameMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The map couldn't be loaded!");
        this.activeMap = loadedLobbyMap.get();
        this.activeInstance.setExplosionSupplier(new ExplosionCreator());
        this.registerInstance(this.activeInstance, map);
        if (this.activeMap.getSpawn() != null) {
            activeInstance.loadChunk(this.activeMap.getSpawn());
        }
        MinecraftServer.getInstanceManager().registerInstance(this.activeInstance);
    }

    public void loadGameChunks() {
        GameMap givenMap = (GameMap) this.activeMap;
        this.activeInstance.loadChunk(givenMap.getSpawn()).join();
    }

    @Override
    public void teleportToSpawn(@NotNull Player player, boolean instanceSet) {
        if (instanceSet) {
            player.setInstance(this.activeInstance, this.activeMap.getSpawn());
            return;
        }
        player.teleport(this.activeMap.getSpawn());
    }

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
}
