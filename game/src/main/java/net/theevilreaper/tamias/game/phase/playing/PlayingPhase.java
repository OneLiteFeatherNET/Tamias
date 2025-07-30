package net.theevilreaper.tamias.game.phase.playing;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.round.event.RoundEndEvent;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.event.AreaCleanupEvent;
import org.jetbrains.annotations.NotNull;

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

    private final IntConsumer timeUpdater;
    private final VoidConsumer startGameLogic;

    /**
     * Creates a new instance of the {@link PlayingPhase} with the given parameters.
     *
     * @param timeUpdater    the updater for time update
     * @param startGameLogic the logic to start the game
     * @param gameListener   a map of listeners which are required for the game
     */
    public PlayingPhase(
            @NotNull IntConsumer timeUpdater,
            @NotNull VoidConsumer startGameLogic,
            @NotNull Supplier<Map<Class<? extends Event>, Consumer<? extends Event>>> gameListener
    ) {
        super("GamePhase", ChronoUnit.SECONDS, 1);
        this.timeUpdater = timeUpdater;
        this.startGameLogic = startGameLogic;
        this.setCurrentTicks(30);
        this.setTickDirection(TickDirection.DOWN);

        for (Map.Entry<Class<? extends Event>, Consumer<? extends Event>> entrySet : gameListener.get().entrySet()) {
            this.addListener((Class<Event>) entrySet.getKey(), (Consumer<Event>) entrySet.getValue());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventDispatcher.call(new AreaCleanupEvent(true));
        this.startGameLogic.apply();
    }

    @Override
    protected void onFinish() {
        EventDispatcher.call(new RoundEndEvent());
    }

    @Override
    public void onUpdate() {
        this.timeUpdater.accept(getCurrentTicks());
    }
}
