package net.theevilreaper.tamias.game.round.event;

import net.minestom.server.event.Event;

/**
 * The {@link RoundEndEvent} is called by the game loop when an active round reaches its end.
 * A reason could be that the time is over or all players of a team are dead.
 * A round is aborted when the game ends early, e.g. because too few players are left. It has no winner then.
 *
 * @param aborted true if the round is aborted, otherwise false
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public record RoundEndEvent(boolean aborted) implements Event {

    /**
     * Creates a new {@link RoundEndEvent} for a round which ended regularly.
     */
    public RoundEndEvent() {
        this(false);
    }
}
