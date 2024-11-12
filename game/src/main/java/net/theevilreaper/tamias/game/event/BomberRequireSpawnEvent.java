package net.theevilreaper.tamias.game.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import org.jetbrains.annotations.NotNull;

public final class BomberRequireSpawnEvent implements PlayerEvent {

    private final Player player;
    private final ExplodeBar explodeBar;

    /**
     * Creates a new instance from this event.
     *
     * @param player     the player who should spawn
     * @param explodeBar the explode bar
     */
    public BomberRequireSpawnEvent(@NotNull Player player, @NotNull ExplodeBar explodeBar) {
        this.player = player;
        this.explodeBar = explodeBar;
    }

    /**
     * Returns the explode bar.
     *
     * @return the explode bar
     */
    public @NotNull ExplodeBar getExplodeBar() {
        return this.explodeBar;
    }

    /**
     * Returns the player who should spawn.
     *
     * @return the player
     */
    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }
}
