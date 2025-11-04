package net.theevilreaper.tamias.setup.dialog.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerDialogRequestEvent implements PlayerEvent {

    private final Player player;
    private final Target target;

    /**
     * Constructs a new PlayerDialogRequestEvent instance.
     *
     * @param player the player who requested the dialog
     * @param target the target of the dialog request
     */
    public PlayerDialogRequestEvent(@NotNull Player player, @NotNull Target target) {
        this.player = player;
        this.target = target;
    }

    /**
     * Gets the target of the dialog request.
     *
     * @return the target of the dialog request
     */
    public @NotNull Target getTarget() {
        return this.target;
    }

    /**
     * Gets the player who requested the dialog.
     *
     * @return the player who requested the dialog
     */
    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    /**
     * Represents the target of the dialog request.
     */
    public enum Target {
        /**
         * The target for the dialog request is the setup name.
         */
        SETUP_NAME,
        /**
         * The target for the dialog request is to define the amount of authors to set up.
         */
        SETUP_REQUEST_AUTHOR,
        /**
         * The target for the dialog request is to define the author of the setup.
         */
        SETUP_AUTHOR,
        /**
         * The target for the dialog request is to set up the block bounce.
         */
        SETUP_BLOCK_BOUNCE

        ;
    }
}
