package net.theevilreaper.tamias.common.map.layer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public record SpawnLayer(@NotNull Pos pos, @NotNull Direction direction) {
}
