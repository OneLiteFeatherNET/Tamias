package net.theevilreaper.tamias.game.phase.playing;

import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;

import java.time.temporal.ChronoUnit;

/**
 * The {@link PostPlayingPhase} closes a round and tears down the game area.
 * It does not care whether another round follows: the preparation of a new round happens at the start of the
 * cycle in {@link net.theevilreaper.tamias.game.phase.GroundBuildPhase}.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PostPlayingPhase extends TimedPhase {

    private static final int POST_PLAYING_TIME = 10;

    public PostPlayingPhase() {
        super("RoundEnd", ChronoUnit.SECONDS, 1);
        this.setPaused(false);
        this.setCurrentTicks(POST_PLAYING_TIME);
        this.setTickDirection(TickDirection.DOWN);
    }

    @Override
    public void onStart() {
        this.setCurrentTicks(POST_PLAYING_TIME);
        super.onStart();
    }

    @Override
    protected void onFinish() {
        EventDispatcher.call(new GameCleanupEvent());
    }

    @Override
    public void onUpdate() {
        // Nothing to do here at the moment
    }
}
