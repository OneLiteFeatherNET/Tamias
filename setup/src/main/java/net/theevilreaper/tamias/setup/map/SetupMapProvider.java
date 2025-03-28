package net.theevilreaper.tamias.setup.map;

import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.common.util.MapFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * SetupMapProvider is responsible for loading and managing maps in the game.
 * It implements the MapProvider and MapFilter interfaces to provide map-related functionalities.
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author theEvilReaper
 */
public final class SetupMapProvider implements MapProvider, MapFilter {

    private static final Pos FALLBACK_POS = new Pos(0, 100, 0);
    private static final Logger FILE_LOGGER = LoggerFactory.getLogger(SetupMapProvider.class);
    private static final String LOBBY_SUFFIX = "lobby"; // Constant for lobby suffix

    private final FileHandler fileHandler;
    private final BaseMap baseMap;
    private final List<MapEntry> maps;
    private final InstanceContainer activeInstance;

    /**
     * Constructs a SetupMapProvider with the specified FileHandler.
     *
     * @param path the path where the maps are stored
     * @param fileHandler the FileHandler used to load and save maps
     */
    public SetupMapProvider(@NotNull Path path, @NotNull FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.maps = loadMapEntries(path.resolve(GameConfig.MAP_FOLDER));
        this.activeInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
        Check.argCondition(this.maps.isEmpty(), "No maps found in the map folder");

        MapEntry mapEntry = this.maps.stream()
                .filter(this::isLobbyMap)
                .findFirst()
                .orElseThrow(() -> new MapLoadingException("No lobby map found in the map folder"));

        this.maps.remove(mapEntry);
        this.baseMap = this.fileHandler.load(mapEntry.getMapFile(), BaseMap.class)
                .orElseThrow(() -> new MapLoadingException("Failed to load map file: " + mapEntry.getMapFile()));

        this.registerInstance(mapEntry);
    }

    /**
     * Registers the specified map entry with the active instance.
     *
     * @param mapEntry the map entry to register
     */
    private void registerInstance(@NotNull MapEntry mapEntry) {
        this.activeInstance.setChunkLoader(new AnvilLoader(mapEntry.getDirectoryRoot()));
        this.activeInstance.enableAutoChunkLoad(true);
        this.activeInstance.setTimeRate(0);
        MinecraftServer.getInstanceManager().registerInstance(this.activeInstance);
    }

    /**
     * Checks if the given map is a lobby map.
     *
     * @param mapEntry the map entry to check
     * @return true if the map is a lobby map
     */
    private boolean isLobbyMap(@NotNull MapEntry mapEntry) {
        return mapEntry.getDirectoryRoot().endsWith(LOBBY_SUFFIX);
    }

    /**
     * Loads all maps from the given path. It filters all directories and creates new {@link MapEntry} instances.
     *
     * @param path the path where the maps are stored
     * @return a list with all available maps
     */
    private @NotNull List<MapEntry> loadMapEntries(@NotNull Path path) {
        List<MapEntry> mapEntries = new ArrayList<>();
        try (Stream<Path> stream = Files.list(path)) {
            mapEntries.addAll(this.filterMapsForSetup(stream.filter(Files::isDirectory)));
        } catch (IOException exception) {
            MinecraftServer.getExceptionManager().handleException(exception);
            FILE_LOGGER.error("Unable to load maps from path {}", path, exception);
        }
        return mapEntries;
    }

    @Override
    public void saveMap(@NotNull Path path, @NotNull BaseMap baseMap) {
        this.fileHandler.save(path, baseMap instanceof GameMap mapToSave ? mapToSave : baseMap);
    }

    @Override
    public void teleportToSpawn(@NotNull Player player, boolean instanceSet) {
        Pos pos = this.baseMap.getSpawnOrDefault(FALLBACK_POS);
        if (!instanceSet) {
            player.teleport(pos);
            return;
        }
        player.setInstance(this.activeInstance, pos);
    }

    @Override
    public @UnmodifiableView @NotNull List<MapEntry> getEntries() {
        return Collections.unmodifiableList(maps);
    }

    @Override
    public @NotNull Supplier<@Nullable Instance> getActiveInstance() {
        return () -> this.activeInstance;
    }

    /**
     * Retrieves the base map currently in use.
     *
     * @return the base map
     */
    public @NotNull BaseMap getBaseMap() {
        return baseMap;
    }

    /**
     * Retrieves the spawn position for the base map.
     *
     * @return the spawn position
     */
    public @NotNull Pos getSpawnPos() {
        return this.baseMap.getSpawnOrDefault(FALLBACK_POS);
    }

    /**
     * Custom exception for map loading errors.
     */
    public static class MapLoadingException extends RuntimeException {
        public MapLoadingException(String message) {
            super(message);
        }
    }
}