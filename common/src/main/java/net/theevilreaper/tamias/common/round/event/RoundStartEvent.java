package net.theevilreaper.tamias.common.round.event;

import net.minestom.server.event.Event;
import net.minestom.server.utils.validate.Check;

public record RoundStartEvent(int round) implements Event {

    public RoundStartEvent {
        Check.argCondition(round <= 0, "The round must start with one");
    }
}
