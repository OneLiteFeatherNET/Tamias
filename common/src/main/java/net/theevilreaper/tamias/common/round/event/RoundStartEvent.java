package net.theevilreaper.tamias.common.round.event;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.Event;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

/**
 * The event is called when a round starts.
 *
 * @param round the round which has started
 */
public record RoundStartEvent(int round, @NotNull Component displayComponent) implements Event {

    /**
     * Creates a new {@link RoundStartEvent} with the given round.
     *
     * @param round the round which has started
     */
    public RoundStartEvent {
        Check.argCondition(round <= 0, "The round must start with one");
    }
}
