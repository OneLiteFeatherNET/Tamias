package net.theevilreaper.tamias.game.stamina;

import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class has some abilities to manage all {@link StaminaBar} references which are required in the game.
 *
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

    /**
     * Creates all {@link StaminaBar} instances for the given {@link TeamService}.
     *
     * @param teamService the service to get the teams
     */
    public void createStaminaObjects(TeamService teamService) {
        // There are just two teams in the game. So this should be fine but when changing the order of the teams this will break
        Team survivorTeam = teamService.getTeams().getFirst();
        Team bomberTeam = teamService.getTeams().getLast();

        for (Player player : survivorTeam.getPlayers()) {
            staminaBars.put(player.getUuid(), StaminaFactory.createShootBar(player));
        }

        for (Player player : bomberTeam.getPlayers()) {
            staminaBars.put(player.getUuid(), StaminaFactory.createExplodeBar(player));
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

    /**
     * Adds a new {@link StaminaBar} for the given {@link UUID}.
     *
     * @param uuid       the unique identifier for the player
     * @param staminaBar the stamina bar to add
     */
    public void add(UUID uuid, StaminaBar staminaBar) {
        lock.lock();
        try {
            staminaBars.put(uuid, staminaBar);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes the {@link StaminaBar} for the given {@link UUID}.
     *
     * @param uuid the unique identifier for the player
     * @return true if the {@link StaminaBar} was removed successfully
     */
    public boolean removeStaminaBar(UUID uuid) {
        try {
            lock.lock();
            StaminaBar staminaBar = staminaBars.get(uuid);
            if (staminaBar != null) {
                staminaBar.stop();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the {@link StaminaBar} for the given {@link Player}.
     *
     * @param player the player to get the stamina bar
     * @return the {@link StaminaBar} or null if the player has no stamina bar
     */
    public @Nullable StaminaBar getStaminaBar(Player player) {
        return this.getStaminaBar(player.getUuid());
    }

    /**
     * Returns the {@link StaminaBar} for the given {@link UUID}.
     *
     * @param uuid the unique identifier for the player
     * @return the {@link StaminaBar} or null if the player has no stamina bar
     */
    public @Nullable StaminaBar getStaminaBar(UUID uuid) {
        try {
            lock.lock();
            return staminaBars.get(uuid);
        } finally {
            lock.unlock();
        }
    }
}
