package net.theevilreaper.tamias.setup.map;

import net.minestom.server.MinecraftServer;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.MapFilter;
import net.theevilreaper.tamias.common.map.provider.AbstractFalcoMapProvider;

import java.nio.file.Path;
import java.util.Optional;

/**
 * SetupMapProvider is responsible for loading and managing maps in the game.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SetupMapProvider extends AbstractFalcoMapProvider {

    private static final String LOBBY_SUFFIX = "lobby"; // Constant for lobby suffix

    /**
     * Constructs a SetupMapProvider with the specified FileHandler.
     *
     * @param path        the path where the maps are stored
     */
    public SetupMapProvider(Path path) {
        super(GsonUtil.FILE_HANDLER, MapFilter::filterMapsForSetup);
        loadMapEntries(path.resolve("setup").resolve("maps"));

        Optional<MapEntry> lobbyEntry = getEntries().stream().filter(this::isLobbyMap).findFirst();

        if (lobbyEntry.isEmpty()) {
            throw new IllegalStateException("No lobby map found in the provided map entries.");
        }

        MapEntry givenMapEntry = lobbyEntry.get();

        if (!givenMapEntry.hasMapFile()) {
            throw new IllegalStateException("The lobby map entry does not have a valid map file.");
        }

        Optional<BaseMap> baseMap = this.fileHandler.load(givenMapEntry.getMapFile(), BaseMap.class);

        if (baseMap.isEmpty()) {
            throw new IllegalStateException("Failed to load the lobby map from the provided entry.");
        }

        this.activeMap = baseMap.get();
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.registerFalcoInstance(this.activeInstance, givenMapEntry);
    }

    /**
     * Checks if the given map is a lobby map.
     *
     * @param mapEntry the map entry to check
     * @return true if the map is a lobby map
     */
    private boolean isLobbyMap(MapEntry mapEntry) {
        return mapEntry.getDirectoryRoot().endsWith(LOBBY_SUFFIX);
    }

    @Override
    public void saveMap(Path path, BaseMap baseMap) {
        this.fileHandler.save(path, baseMap instanceof GameMap mapToSave ? mapToSave : baseMap);
    }
}


