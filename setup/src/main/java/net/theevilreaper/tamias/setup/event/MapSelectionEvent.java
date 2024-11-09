package net.theevilreaper.tamias.setup.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class MapSelectionEvent implements PlayerEvent {

    private final Player player;
    private final Path path;

    public MapSelectionEvent(@NotNull Player player, @NotNull Path entry) {
        this.player = player;
        this.path = entry;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Path getPath() {
        return path;
    }
}
