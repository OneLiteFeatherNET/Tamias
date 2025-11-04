package net.theevilreaper.tamias.setup.dialog.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import net.theevilreaper.tamias.setup.inventory.DataType;
import org.jetbrains.annotations.NotNull;

public class PlayerDeletePromptEvent implements PlayerEvent {

    private final Player player;
    private final DataType type;

    public PlayerDeletePromptEvent(@NotNull Player player, @NotNull DataType type) {
        this.player = player;
        this.type = type;
    }

    /**
     * The involved data type
     *
     * @return the type of overview
     */
    public @NotNull DataType getType() {
        return type;
    }

    /**
     * Gets the player who triggered the delete prompt event.
     *
     * @return the player who triggered the event
     */
    public @NotNull Player getPlayer() {
        return this.player;
    }
}
