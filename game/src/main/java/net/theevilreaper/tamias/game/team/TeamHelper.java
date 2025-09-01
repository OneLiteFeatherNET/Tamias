package net.theevilreaper.tamias.game.team;

import net.theevilreaper.aves.util.Players;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.xerus.api.ColorData;
import net.theevilreaper.xerus.api.component.team.ColorComponent;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class TeamHelper {

    /**
     * Loads the teams into the team service.
     * The survivor team is green and the bomber team is red.
     *
     * @param teamSize    the size of each team
     * @param teamService the service which provides access to the teams
     */
    public static void loadTeams(int teamSize, @NotNull TeamService teamService) {
        // Avoid loading teams multiple times
        if (teamService.hasTeams()) return;
        Team survivorTeam = new TamiasTeam(GameConfig.SURVIVOR_KEY, teamSize, GameConfig.SURVIVOR_ID);
        survivorTeam.add(ColorComponent.class, new ColorComponent(ColorData.GREEN));
        teamService.add(survivorTeam);

        Team bomberTeam = new TamiasTeam(GameConfig.BOMBER_KEY, teamSize, GameConfig.TNT_ID);
        bomberTeam.add(ColorComponent.class, new ColorComponent(ColorData.RED));
        teamService.add(bomberTeam);
    }

    public static void switchToTNTTeam(@NotNull IntFunction<Team> teamIntFunction, @NotNull Player player) {
        Check.argCondition(!player.hasTag(Tags.TEAM_ID), "Need a team tag for switching teams");

        int id = player.getTag(Tags.TEAM_ID);

        Check.argCondition(GameConfig.SURVIVOR_ID != id, "The player must be a survivor");

        Team survivorTeam = teamIntFunction.apply(id);
        Team bomberTeam = teamIntFunction.apply(GameConfig.TNT_ID);

        survivorTeam.removePlayer(player, involved -> involved.getInventory().clear());
        bomberTeam.addPlayer(player);
        EventDispatcher.call(new RoleToBomberChangeEvent(player));
    }

    public static void allocateTeams(@NotNull TeamService teamService) {
        Check.argCondition(!teamService.hasTeams(), "The team service must contain teams");

        final Set<Player> onlinePlayers = new HashSet<>(MinecraftServer.getConnectionManager().getOnlinePlayers());
        // Check.argCondition(onlinePlayers.size() < 2, "The player list must contain at least two players");

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
    public static void teleport(@NotNull TeamService teamService, @NotNull GameMap mapSupplier, @NotNull PlayerConsumer playerConsumer) {
        Team bomberTeam = teamService.getTeams().get(GameConfig.TNT_ID);
        Team survivorTeam = teamService.getTeams().get(GameConfig.SURVIVOR_ID);

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
        // Check.argCondition(team.getPlayers().isEmpty(), "The given team can't be empty");
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
