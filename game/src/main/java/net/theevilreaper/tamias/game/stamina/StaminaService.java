package net.theevilreaper.tamias.game.stamina;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class has some abilities to manage all {@link StaminaBar} references which are required in the game.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class StaminaService {

    private final Lock lock;
    private final Map<UUID, StaminaBar> staminaBars;

    /**
     * Creates a new instance from this class.
     */
    public StaminaService() {
        this.lock = new ReentrantLock();
        this.staminaBars = new HashMap<>();
    }

    public void addStaminas(@NotNull Map<UUID, StaminaBar> staminaBars) {
        lock.lock();
        try {
            this.staminaBars.putAll(staminaBars);
        } finally {
            lock.unlock();
        }
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

    public @Nullable StaminaBar getStaminaBar(@NotNull UUID uuid) {
        try {
            lock.lock();
            return staminaBars.get(uuid);
        } finally {
            lock.unlock();
        }
    }
}
