package net.theevilreaper.tamias.common.round.event;

import net.minestom.server.event.Event;
import net.minestom.server.utils.validate.Check;

/**
 * The event is called when a round ends.
 *
 * @param winnerTeam the team which has won the round
 */
public record RoundEndEvent(byte winnerTeam) implements Event {

    /**
     * Creates a new {@link RoundEndEvent} with the given winner team.
     *
     * @param winnerTeam the team which has won the round
     */
    public RoundEndEvent {
        Check.argCondition(winnerTeam <= 0, "The winner team must be greater than 0");
    }
}
