package net.theevilreaper.tamias.game.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class providing non-blocking teleportation logic for teams and players.
 *
 * @author theEvilReaper
 * @version 1.1.0
 * @since 1.0.0
 */
public final class TeleportationLogic {

    /**
     * Teleports the players of the given team service to their initial spawn positions on the map.
     *
     * @param teamService    the team service
     * @param map            the game map providing spawn positions
     * @param playerConsumer optional callback for each player
     * @return a {@link CompletableFuture} completing when all teleports finish
     */
    public static CompletableFuture<Void> teleport(TeamService teamService, GameMap map, @Nullable PlayerConsumer playerConsumer) {
        Optional<Team> bomberTeamOpt = teamService.getTeam(GameConfig.BOMBER_KEY);
        Optional<Team> survivorTeamOpt = teamService.getTeam(GameConfig.SURVIVOR_KEY);

        CompletableFuture<Void> bomberFuture = bomberTeamOpt
                .map(team -> teleport(team, map.getBomberInitialSpawn(), playerConsumer))
                .orElseGet(() -> CompletableFuture.completedFuture(null));

        CompletableFuture<Void> survivorFuture = survivorTeamOpt
                .map(team -> teleport(team, map.spawn(), playerConsumer))
                .orElseGet(() -> CompletableFuture.completedFuture(null));

        return CompletableFuture.allOf(bomberFuture, survivorFuture);
    }

    /**
     * Teleports all players of a team to a given position.
     *
     * @param team the team to teleport
     * @param pos  the position to teleport players to
     * @return a {@link CompletableFuture} completing when all teleports finish
     */
    public static CompletableFuture<Void> teleport(Team team, Pos pos) {
        return teleport(team, pos, null);
    }

    /**
     * Teleports all players of a team to a given position with an optional callback.
     *
     * @param team     the team to teleport
     * @param pos      the position to teleport players to
     * @param callback optional callback executed per player after teleportation
     * @return a {@link CompletableFuture} completing when all teleports finish
     */
    public static CompletableFuture<Void> teleport(Team team, Pos pos, @Nullable PlayerConsumer callback) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Player player : team.getPlayers()) {
            CompletableFuture<Void> future = player.teleport(pos);
            if (callback != null) {
                future = future.thenAccept(_ -> callback.accept(player));
            }
            futures.add(future);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private TeleportationLogic() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
