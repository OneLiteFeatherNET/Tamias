package net.theevilreaper.tamias.common.map;

import net.theevilreaper.aves.map.MapEntry;
import net.theevilreaper.tamias.common.config.GameConfig;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * The class is a utility class that provides a method to filter through a stream of paths and returns a list of maps.
 * It is used to filter the available maps for the game and setup.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MapFilter {

    String REGION_FOLDER = "region";

    /**
     * Filters through the given stream of paths and returns a list of maps which are available for the game.
     *
     * @param mapStream a stream of paths
     * @return a list that contains different maps which are available for the game
     */
    static @Unmodifiable List<MapEntry> filterMapsForGame(Stream<Path> mapStream) {
        return mapStream
                .filter(Files::isDirectory)
                .filter(path -> Files.exists(path.resolve(REGION_FOLDER)))
                .filter(path -> Files.exists(path.resolve(GameConfig.MAP_FILE_NAME)))
                .map(MapEntry::of)
                .toList();
    }

    /**
     * Filters through the given stream of paths and returns a list of maps which are available for the setup.
     *
     * @param mapStream a stream of paths
     * @return a list that contains different maps which are available for the setup
     */
    static @Unmodifiable List<MapEntry> filterMapsForSetup(Stream<Path> mapStream) {
        return mapStream
                .filter(Files::isDirectory)
                .filter(path -> Files.exists(path.resolve(REGION_FOLDER)))
                .map(MapEntry::of)
                .toList();
    }
}
