package net.theevilreaper.tamias.game.util;

import net.theevilreaper.tamias.common.config.GameConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileChecker.class);

    public static void checkFileIntegrity(@NotNull Path rootPath) {
        var mapDirectory = rootPath.resolve(GameConfig.MAP_FOLDER);
        if (!Files.exists(rootPath)) {
            try {
                Files.createDirectory(rootPath);
            } catch (IOException exception) {
                LOGGER.error("Unable to create extension directory", exception);
            }
        }

        if (!Files.exists(mapDirectory)) {
            try {
                Files.createDirectory(mapDirectory);
            } catch (IOException exception) {
                LOGGER.error("Unable to create the map folder", exception);
            }
        }
    }

    private FileChecker() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
