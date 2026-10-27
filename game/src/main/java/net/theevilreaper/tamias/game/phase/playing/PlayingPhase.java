package net.theevilreaper.tamias.game.phase.playing;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.event.SpawnCleanupEvent;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayingPhase extends TimedPhase {

    private final int playingTime;
    private final IntConsumer timeUpdater;
    private final VoidConsumer startGameLogic;

    /**
     * Creates a new instance of the {@link PlayingPhase} with the given parameters.
     *
     * @param playingTime    the duration of the round in seconds
     * @param timeUpdater    the updater for time update
     * @param startGameLogic the logic to start the game
     * @param gameListener   a map of listeners which are required for the game
     */
    public PlayingPhase(
            int playingTime,
            IntConsumer timeUpdater,
            VoidConsumer startGameLogic,
            Supplier<Map<Class<? extends Event>, Consumer<? extends Event>>> gameListener
    ) {
        super("GamePhase", ChronoUnit.SECONDS, 1);
        this.playingTime = playingTime;
        this.timeUpdater = timeUpdater;
        this.startGameLogic = startGameLogic;
        this.setCurrentTicks(playingTime);
        this.setTickDirection(TickDirection.DOWN);

        for (Map.Entry<Class<? extends Event>, Consumer<? extends Event>> entrySet : gameListener.get().entrySet()) {
            this.addListener((Class<Event>) entrySet.getKey(), (Consumer<Event>) entrySet.getValue());
        }
    }

    @Override
    public void onStart() {
        this.setCurrentTicks(this.playingTime);
        super.onStart();
        EventDispatcher.call(new SpawnCleanupEvent());
        this.timeUpdater.accept(this.playingTime);
        this.startGameLogic.apply();
    }

    @Override
    protected void onFinish() {
        // The phase is skipping when the game ends early, the round is aborted then
        EventDispatcher.call(new RoundEndEvent(isSkipping()));
    }

    @Override
    public void onUpdate() {
        this.timeUpdater.accept(getCurrentTicks());
    }
}
