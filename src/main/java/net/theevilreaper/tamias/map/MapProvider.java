package net.theevilreaper.tamias.map;

import com.google.gson.Gson;
import de.icevizion.aves.file.FileHandler;
import de.icevizion.aves.file.GsonFileHandler;
import de.icevizion.aves.map.BaseMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.area.SpawnArea;
import net.theevilreaper.tamias.config.GameConfig;
import net.theevilreaper.tamias.explosion.ExplosionCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public final class MapProvider {

    private static final String LOBBY_FOLDER_NAME = "lobby";
    private final Path mapPath;
    private final FileHandler fileHandler;

    private BaseMap lobbyMap;
    private GameMap gameMap;
    private InstanceContainer gameMapInstance;
    private List<Path> maps;
    private SpawnArea spawnArea;

    public MapProvider(@NotNull Gson gson, @NotNull Path originPath, @NotNull InstanceContainer instanceContainer) {
        this.mapPath = originPath.resolve(GameConfig.MAP_PATH_NAME);
        this.fileHandler = new GsonFileHandler(gson);
        this.loadMapPaths();
        //this.loadLobbyMap(instanceContainer);
    }

    private void loadMapPaths() {
        try (Stream<Path> paths = Files.list(mapPath)) {
            this.maps = new ArrayList<>(paths.filter(Files::isDirectory).toList());
        } catch (IOException exception) {
            MinecraftServer.getExceptionManager().handleException(exception);
        }
    }


    /**
     * Tries to load the lobby map from the map folder.
     * @param instanceContainer the instance container to bind the directory to it
     */
    private void loadLobbyMap(@NotNull InstanceContainer instanceContainer) {
        Check.argCondition(this.maps.isEmpty(), "Can't load lobby because the maps folder contains no lobby");

        Path lobbyPath = null;

        for (int i = 0; i < this.maps.size() && lobbyPath == null; i++) {
            var currentMap = this.maps.get(i);
            if (!LOBBY_FOLDER_NAME.equals(currentMap.getFileName().toString())) continue;
            lobbyPath = currentMap;
        }

        Check.argCondition(lobbyPath == null, "The map folder contains no lobby map!");
        System.out.println("Lobby path: " + lobbyPath);
        /*Optional<BaseMap> loadedLobbyMap = fileHandler.load(lobbyPath, BaseMap.class);
        Check.argCondition(loadedLobbyMap.isEmpty(), "The lobby map couldn't be loaded!");
        this.lobbyMap = loadedLobbyMap.get();*/

        instanceContainer.setChunkLoader(new AnvilLoader(lobbyPath));
        instanceContainer.enableAutoChunkLoad(true);

        // We need to remove the lobby path from the maps to prevent that the lobby map is a candidate for the game
        this.maps.remove(lobbyPath);
        this.gameMapInstance = instanceContainer;
    }

    public @NotNull InstanceContainer loadGameMap() {
        if (this.gameMapInstance != null) return gameMapInstance;
        Check.argCondition(this.maps.size() < 2, "NOPE");
        Path path;
        Collections.shuffle(this.maps);
        if (this.maps.size() == 1) {
            path = this.maps.get(0);
        } else {
            path = this.maps.get(ThreadLocalRandom.current().nextInt(this.maps.size()));
        }

        Check.argCondition(path == null, "Unable to load game map");

        var loader = new AnvilLoader(path);

        var mapOptional = fileHandler.load(path, GameMap.class);

        Check.argCondition(mapOptional.isEmpty(), "Something went wrong during map load");

        this.gameMap = mapOptional.get();

        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();
        container.setChunkLoader(loader);
        container.setExplosionSupplier(new ExplosionCreator());
        container.setTimeRate(0);
        container.setTimeUpdate(null);
        return container;
    }

    public <T extends BaseMap> @Nullable Optional<T> loadMap(@NotNull Path path, @NotNull Class<T> mapObject) {
        return this.fileHandler.load(path, mapObject);
    }

    public <T extends BaseMap> void saveMap(@NotNull String name, @NotNull T mapObject) {
        this.fileHandler.save(mapPath.resolve(name), mapObject);
    }

    public @Nullable BaseMap getActiveMap() {
        if (lobbyMap != null) return lobbyMap;
        if (gameMap != null) return gameMap;
        return null;
    }
}
