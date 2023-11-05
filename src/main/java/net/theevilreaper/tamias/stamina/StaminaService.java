package net.theevilreaper.tamias.stamina;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The class has some abilities to manage all {@link StaminaBar} references which are required in the game.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class StaminaService {

    private final Map<UUID, StaminaBar> staminaBars;

    /**
     * Creates a new instance from this class.
     */
    public StaminaService() {
        this.staminaBars = new HashMap<>();
    }

    /**
     * Starts all {@link net.minestom.server.timer.Task} reference from each {@link StaminaBar}.
     */
    public void start() {
        for (StaminaBar value : this.staminaBars.values()) {
            value.start();
        }
    }

    /**
     * Stops all running {@link StaminaBar} instances.
     */
    public void cleanUp() {
        if (staminaBars.isEmpty()) return;
        for (StaminaBar value : staminaBars.values()) {
            value.stop();
        }
        staminaBars.clear();
    }

}
