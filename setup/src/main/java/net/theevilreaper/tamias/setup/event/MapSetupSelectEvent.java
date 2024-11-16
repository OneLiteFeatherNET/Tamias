package net.theevilreaper.tamias.setup.event;

import de.icevizion.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.setup.state.SetupState;
import org.jetbrains.annotations.NotNull;

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
    private final SetupState setupMode;
    private boolean cancelled;

    /**
     * Creates a new instance of the {@link MapSetupSelectEvent}.
     *
     * @param player    the player who selected the map
     * @param mapEntry  the selected map
     * @param setupMode the current setup mode
     */
    public MapSetupSelectEvent(@NotNull Player player, @NotNull MapEntry mapEntry, @NotNull SetupState setupMode) {
        this.player = player;
        this.mapEntry = mapEntry;
        this.setupMode = setupMode;
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
    public @NotNull MapEntry getMapEntry() {
        return mapEntry;
    }

    /**
     * Returns the current setup mode.
     *
     * @return the current mode
     */
    public @NotNull SetupState getSetupMode() {
        return setupMode;
    }

    /**
     * Returns the player who selected the map.
     *
     * @return the player
     */
    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }
}
