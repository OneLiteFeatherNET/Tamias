package net.theevilreaper.tamias.game.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link BomberRequireSpawnEvent} is called by the game everytime when a bomber player wants to spawn at a new tnt.
 * When all positions are taken it is not possible to respawn.
 *
 * @author 1.0.0
 * @author theEvilReaper
 * @version 1.0.0
 */
public final class BomberRequireSpawnEvent implements PlayerEvent, CancellableEvent {

    private final Player player;
    private final ExplodeBar explodeBar;
    private boolean cancelled;

    /**
     * Creates a new instance from this event.
     *
     * @param player     the player who should spawn
     * @param explodeBar the reference to the {@link ExplodeBar}
     */
    public BomberRequireSpawnEvent(@NotNull Player player, @NotNull ExplodeBar explodeBar) {
        this.player = player;
        this.explodeBar = explodeBar;
    }

    /**
     * Updates the cancellation state of the event.
     *
     * @param cancelled true if the event should be cancelled, false otherwise
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Returns the cancellation state from the event.
     *
     * @return the state
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Returns involved reference of the {@link ExplodeBar}.
     *
     * @return the reference
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
