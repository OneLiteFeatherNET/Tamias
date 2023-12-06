package net.theevilreaper.tamias.team;

import de.icevizion.aves.util.Players;
import de.icevizion.xerus.api.team.Team;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.config.GameConfig;
import net.theevilreaper.tamias.map.GameMap;
import net.theevilreaper.tamias.map.MapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class TeamHelper {

    private final MapProvider mapProvider;
    private final Function<Byte, @NotNull Team> teamMapper;
    private final Team survivorTeam;

    public TeamHelper(@NotNull MapProvider mapProvider, @NotNull Function<Byte, @NotNull Team> teamMapper) {
        this.mapProvider = mapProvider;
        this.teamMapper = teamMapper;
        this.survivorTeam = teamMapper.apply(GameConfig.SURVIVOR_ID);
    }

    public void removeSurvivor(@NotNull Player player) {
        this.survivorTeam.removePlayer(player);
    }

    public void addTNT(@NotNull Player player) {
        this.teamMapper.apply(GameConfig.TNT_ID).addPlayer(player);
    }

    public boolean hasSurvivorLeft() {
        return this.survivorTeam.isEmpty();
    }

    public void allocateTeams(@NotNull Set<Player> players) {
        Check.argCondition(players.isEmpty(), "The player list cannot be empty");
        Check.argCondition(players.size() < 2, "The player list must contain at least two players");
        final Player randomPlayer = Players.getRandomPlayer(new ArrayList<>(players)).get();
        players.removeIf(player -> player.getUuid().equals(randomPlayer.getUuid()));
        teamMapper.apply(GameConfig.TNT_ID).addPlayer(randomPlayer);
        this.survivorTeam.addPlayers(players);
    }

    public void teleport() {
        if (mapProvider.getActiveMap() == null) return;
        var tntTeam = teamMapper.apply(GameConfig.TNT_ID);
        Check.argCondition(tntTeam.getPlayers().isEmpty(), "The tnt team cannot be empty");
        Check.argCondition(tntTeam.getPlayers().size() > 1, "The tnt team must contain only one player at start");
        Check.argCondition(survivorTeam.getPlayers().isEmpty(), "The survivor team cannot be empty");

        var gameMap = ((GameMap)mapProvider.getActiveMap());

        tntTeam.getPlayers().forEach(player -> player.teleport(gameMap.getBomberInitialSpawn()));
        survivorTeam.getPlayers().forEach(player -> player.teleport(gameMap.getSpawn()));
    }

    public void clearTeams() {
        teamMapper.apply(GameConfig.TNT_ID).clearPlayers();
        teamMapper.apply(GameConfig.SURVIVOR_ID).clearPlayers();
    }
}
