package net.theevilreaper.tamias.game.event;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class BomberExplodeEvent implements PlayerEvent {

    private final Player player;
    private final Vec vec;

    public BomberExplodeEvent(@NotNull Player player, @NotNull Vec vec) {
        this.player = player;
        this.vec = vec;
    }

    @NotNull Vec getPosition() {
        return this.vec;
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }
}
