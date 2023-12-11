package net.theevilreaper.tamias.setup.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class MapSavedEvent implements PlayerEvent {

    private final Player player;

    public MapSavedEvent(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
