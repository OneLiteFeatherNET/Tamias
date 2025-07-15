package net.theevilreaper.tamias.setup.event.dialog;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link SetupDialogRequestEvent} is triggered when a player requests to open a setup dialog.
 * This event is cancellable, allowing the server to prevent the dialog from being displayed.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public class SetupDialogRequestEvent implements PlayerEvent, CancellableEvent {

    private final Player player;
    private final Type type;
    private boolean cancelled;

    /**
     * Constructs a new {@link SetupDialogRequestEvent}.
     *
     * @param player the player who triggered the event
     * @param type   the type of dialog to be opened
     */
    public SetupDialogRequestEvent(@NotNull Player player, @NotNull Type type) {
        this.player = player;
        this.type = type;
        this.cancelled = false;
    }

    /**
     * Sets whether the event is cancelled.
     *
     * @param cancel true to cancel the event, false otherwise
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Checks if the event has been cancelled.
     *
     * @return true if the event is cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Gets the player who requested the setup dialog.
     *
     * @return the player instance
     */
    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the type of dialog that was requested.
     *
     * @return the type of dialog
     */
    public @NotNull Type getType() {
        return type;
    }

    /**
     * Enum representing the types of setup dialogs that can be requested.
     *
     * @author theEvilReaper
     * @version 1.0.0
     * @since 0.1.0
     */
    public enum Type {

        MAP_NAME,
        MAP_AUTHORS,
    }
}
