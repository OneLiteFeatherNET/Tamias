package net.theevilreaper.tamias.setup.event;

import net.theevilreaper.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * The {@link MapSetupSelectEvent} is called when a player selects a map for the setup process.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MapSetupSelectEvent implements PlayerEvent, CancellableEvent {

    private final Player player;
    private final MapEntry mapEntry;
    private final boolean lobbyMode;
    private boolean cancelled;

    /**
     * Creates a new instance of the {@link MapSetupSelectEvent}.
     *
     * @param player    the player who selected the map
     * @param mapEntry  the selected map
     * @param lobbyMode if the setup is for the lobby or the game
     */
    public MapSetupSelectEvent(Player player, MapEntry mapEntry, boolean lobbyMode) {
        this.player = player;
        this.mapEntry = mapEntry;
        this.lobbyMode = lobbyMode;
    }

    /**
     * Checks if the event is cancelled.
     *
     * @return true if the event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets the cancellation state for the event.
     *
     * @param cancel the new state for the event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Returns the selected map entry.
     *
     * @return the map entry
     */
    public MapEntry getMapEntry() {
        return mapEntry;
    }

    /**
     * Returns if the setup is for the lobby or the game.
     * @return true if the setup is for the lobby
     */
    public boolean isLobbyMode() {
        return lobbyMode;
    }

    /**
     * Returns the player who selected the map.
     *
     * @return the player
     */
    @Override
    public Player getPlayer() {
        return this.player;
    }
}
