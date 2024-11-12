package net.theevilreaper.tamias.common.round.event;

import net.minestom.server.event.Event;
import net.minestom.server.utils.validate.Check;

/**
 * The event is called when a round starts.
 *
 * @param round the round which has started
 */
public record RoundStartEvent(int round) implements Event {

    /**
     * Creates a new {@link RoundStartEvent} with the given round.
     *
     * @param round the round which has started
     */
    public RoundStartEvent {
        Check.argCondition(round <= 0, "The round must start with one");
    }
}
