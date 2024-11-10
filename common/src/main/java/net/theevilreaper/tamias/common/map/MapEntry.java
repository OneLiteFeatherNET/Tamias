package net.theevilreaper.tamias.common.map;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

// Maps as path
public record MapEntry(@NotNull Path path) {

    public boolean hasMapFile() {
        return Files.exists(path.resolve("map.json"));
    }

    public @NotNull Path getMapFile() {
        return path.resolve("map.json");
    }
}
