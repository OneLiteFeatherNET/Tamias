package net.theevilreaper.tamias.game.event.placement;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link TriggerPlacementEvent} is called when a specific placement should be triggered.
 *
 * @param type the type of the trigger placement event
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public record TriggerPlacementEvent(@NotNull Type type) implements Event {

    /**
     * The type of the trigger placement event.
     *
     * @author theEvilReaper
     * @version 1.0.0
     * @since 0.1.0
     */
    public enum Type {
        // The trigger is being placed in the spawn area
        SPAWN,
        // The trigger is being placed in the game area
        GAME
    }
}
