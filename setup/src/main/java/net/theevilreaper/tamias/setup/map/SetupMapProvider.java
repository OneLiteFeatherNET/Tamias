package net.theevilreaper.tamias.setup.map;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.aves.file.GsonFileHandler;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.aves.map.provider.AbstractMapProvider;
import net.theevilreaper.tamias.common.gson.GsonUtil;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.functional.LobbyMapPredicate;
import net.theevilreaper.tamias.common.util.MapFilter;

import java.nio.file.Path;
import java.util.Optional;

/**
 * SetupMapProvider is responsible for loading and managing maps in the game.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SetupMapProvider extends AbstractMapProvider {

    private static final Pos FALLBACK_POS = new Pos(0, 100, 0);

    /**
     * Constructs a SetupMapProvider with the specified FileHandler.
     *
     * @param path        the path where the maps are stored
     */
    public SetupMapProvider(Path path) {
        super(new GsonFileHandler(GsonUtil.GSON), MapFilter::filterMapsForSetup);
        this.mapEntries = loadMapEntries(path.resolve("maps"));

        LobbyMapPredicate predicate = new LobbyMapPredicate();
        Optional<MapEntry> lobbyEntry = getEntries().stream().filter(predicate).findFirst();

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
        this.registerInstance(this.activeInstance, lobbyEntry.get());
    }

    @Override
    public void saveMap(Path path, BaseMap baseMap) {
        this.fileHandler.save(path, baseMap instanceof GameMap mapToSave ? mapToSave : baseMap);
    }

    @Override
    public void teleportToSpawn(Player player, boolean instanceSet) {
        Pos pos = activeMap.getSpawnOrDefault(FALLBACK_POS);
        if (!instanceSet) {
            player.teleport(pos);
            return;
        }
        player.setInstance(this.activeInstance, pos);
    }
}