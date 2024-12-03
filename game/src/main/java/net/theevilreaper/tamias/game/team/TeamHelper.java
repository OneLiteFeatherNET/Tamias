package net.theevilreaper.tamias.game.team;

import de.icevizion.aves.util.Players;
import de.icevizion.aves.util.functional.PlayerConsumer;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.GameMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class TeamHelper {

    public static void allocateTeams(@NotNull TeamService<Team> teamService) {
        Check.argCondition(!teamService.hasTeams(), "The team service must contain teams");

        final Set<Player> onlinePlayers = new HashSet<>(MinecraftServer.getConnectionManager().getOnlinePlayers());
        Check.argCondition(onlinePlayers.size() < 2, "The player list must contain at least two players");

        final Optional<Player> randomPlayer = Players.getRandomPlayer();
        Check.argCondition(randomPlayer.isEmpty(), "No random player found");

        final Player player = randomPlayer.get();
        onlinePlayers.remove(player);

        // Bomber team is the last team
        teamService.getTeams().getLast().addPlayer(player);
        teamService.getTeams().getFirst().addPlayers(onlinePlayers);
    }

    /**
     * Teleports the players of the given team to the initial spawn position.
     * The bomber team will be teleported to the bomber spawn and the survivor team to the survivor spawn.
     * Each position is provided by the {@link GameMap}.
     *
     * @param teamService the service which contains the teams
     * @param mapSupplier the supplier which provides the map
     */
    public static void teleport(@NotNull TeamService<Team> teamService, @NotNull GameMap mapSupplier, @NotNull PlayerConsumer playerConsumer) {
        Team bomberTeam = teamService.getTeams().get(GameConfig.TNT_ID);
        Team survivorTeam = teamService.getTeams().get(GameConfig.SURVIVOR_ID);
        Check.argCondition(bomberTeam.getPlayers().isEmpty(), "The tnt team cannot be empty");
        Check.argCondition(survivorTeam.getPlayers().isEmpty(), "The survivor team cannot be empty");

        teleport(bomberTeam, mapSupplier.getBomberInitialSpawn(), playerConsumer);
        teleport(survivorTeam, mapSupplier.getSpawn(), playerConsumer);
    }

    public static void teleport(@NotNull Team team, @NotNull Pos pos) {
        teleport(team, pos, null);
    }

    /**
     * Teleports the players of the given team to the given position.
     *
     * @param team the team to teleport
     * @param pos  the position to teleport the players
     */
    private static void teleport(@NotNull Team team, @NotNull Pos pos, @Nullable PlayerConsumer callback) {
        team.getPlayers().forEach(player -> {
            player.teleport(pos).join();
            if (callback != null) {
                callback.accept(player);
            }
        });
    }

    private TeamHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
