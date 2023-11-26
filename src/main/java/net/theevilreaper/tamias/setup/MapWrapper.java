package net.theevilreaper.tamias.setup;

import de.icevizion.aves.map.BaseMap;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record MapWrapper(@NotNull BaseMap map, @NotNull Path path) {}
