package net.theevilreaper.tamias.bridge;

import net.theevilreaper.tamias.common.event.GameStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Reacts to {@link GameStartEvent} by switching the service into its in game state, once.
 * <p>
 * Failures are logged and never propagate into the game thread.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 1.0.0
 */
public final class InGameListener implements Consumer<GameStartEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InGameListener.class);

    private final ServiceStateSwitch stateSwitch;
    private final AtomicBoolean switched = new AtomicBoolean();

    /**
     * Creates a new listener.
     *
     * @param stateSwitch the switch to trigger
     */
    public InGameListener(ServiceStateSwitch stateSwitch) {
        this.stateSwitch = stateSwitch;
    }

    @Override
    public void accept(GameStartEvent event) {
        if (!switched.compareAndSet(false, true)) return;
        try {
            stateSwitch.switchToInGame();
        } catch (Throwable throwable) {
            LOGGER.error("Could not switch the service to its in game state", throwable);
        }
    }
}
