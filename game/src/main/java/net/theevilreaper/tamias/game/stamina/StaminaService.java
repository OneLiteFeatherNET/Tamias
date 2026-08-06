package net.theevilreaper.tamias.game.stamina;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.team.component.StaminaComponent;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all {@link StaminaBar} references required during game execution in a thread-safe manner.
 *
 * @author theEvilReaper
 * @version 1.1.0
 * @since 1.0.0
 */
public final class StaminaService {

    private final Map<UUID, StaminaBar> staminaBars;

    /**
     * Creates a new instance of {@link StaminaService}.
     */
    public StaminaService() {
        this.staminaBars = new ConcurrentHashMap<>();
    }

    /**
     * Creates all {@link StaminaBar} instances for players registered in the given {@link TeamService}.
     *
     * @param teamService the team service providing teams and players
     */
    public void createStaminaObjects(TeamService teamService) {
        for (Team team : teamService.getTeams()) {
            StaminaComponent staminaComp = team.get(StaminaComponent.class);
            if (staminaComp == null) continue;
            for (Player player : team.getPlayers()) {
                this.staminaBars.put(player.getUuid(), staminaComp.staminaFactory().apply(player));
            }
        }
    }

    /**
     * Starts all registered {@link StaminaBar} instances.
     */
    public void start() {
        for (StaminaBar staminaBar : this.staminaBars.values()) {
            staminaBar.start();
        }
    }

    /**
     * Stops and clears all running {@link StaminaBar} instances.
     */
    public void cleanUp() {
        if (this.staminaBars.isEmpty()) return;
        for (StaminaBar staminaBar : this.staminaBars.values()) {
            staminaBar.stop();
        }
        this.staminaBars.clear();
    }

    /**
     * Adds a new {@link StaminaBar} for the given player {@link UUID}.
     *
     * @param uuid       the unique identifier of the player
     * @param staminaBar the stamina bar instance
     */
    public void add(UUID uuid, StaminaBar staminaBar) {
        this.staminaBars.put(uuid, staminaBar);
    }

    /**
     * Removes and stops the {@link StaminaBar} associated with the given {@link UUID}.
     *
     * @param uuid the unique identifier of the player
     * @return true if a stamina bar was removed and stopped, false otherwise
     */
    public boolean removeStaminaBar(UUID uuid) {
        StaminaBar staminaBar = this.staminaBars.remove(uuid);
        if (staminaBar != null) {
            staminaBar.stop();
            return true;
        }
        return false;
    }

    /**
     * Returns the {@link StaminaBar} for the given {@link Player}.
     *
     * @param player the player
     * @return the {@link StaminaBar} or null if no stamina bar exists for the player
     */
    public @Nullable StaminaBar getStaminaBar(Player player) {
        return this.getStaminaBar(player.getUuid());
    }

    /**
     * Returns the {@link StaminaBar} for the given {@link UUID}.
     *
     * @param uuid the unique identifier of the player
     * @return the {@link StaminaBar} or null if no stamina bar exists for the UUID
     */
    public @Nullable StaminaBar getStaminaBar(UUID uuid) {
        return this.staminaBars.get(uuid);
    }
}
