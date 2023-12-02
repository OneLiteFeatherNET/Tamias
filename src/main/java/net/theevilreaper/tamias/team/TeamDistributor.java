package net.theevilreaper.tamias.team;

import de.icevizion.aves.util.Players;
import de.icevizion.xerus.api.team.Team;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.config.GameConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class TeamDistributor {

    private final Pos survivorSpawn;
    private final Pos tntSpawn;
    private final Function<Byte, @NotNull Team> teamMapper;

    public TeamDistributor(@NotNull Pos survivorSpawn, @NotNull Pos tntSpawn, @NotNull Function<Byte, @NotNull Team> teamMapper) {
        this.survivorSpawn = survivorSpawn;
        this.tntSpawn = tntSpawn;
        this.teamMapper = teamMapper;
    }

    public void allocateTeams(@NotNull Set<Player> players) {
        Check.argCondition(players.isEmpty(), "The player list cannot be empty");
        Check.argCondition(players.size() < 2, "The player list must contain at least two players");
        final Player randomPlayer = Players.getRandomPlayer(new ArrayList<>(players)).get();
        players.removeIf(player -> player.getUuid().equals(randomPlayer.getUuid()));
        teamMapper.apply(GameConfig.TNT_ID).addPlayer(randomPlayer);
        teamMapper.apply(GameConfig.SURVIVOR_ID).addPlayers(players);
    }

    public void teleport() {
        var tntTeam = teamMapper.apply(GameConfig.TNT_ID);
        Check.argCondition(tntTeam.getPlayers().isEmpty(), "The tnt team cannot be empty");
        Check.argCondition(tntTeam.getPlayers().size() > 1, "The tnt team must contain only one player at start");
        var survivorTeam = teamMapper.apply(GameConfig.SURVIVOR_ID);
        Check.argCondition(survivorTeam.getPlayers().isEmpty(), "The survivor team cannot be empty");

        tntTeam.getPlayers().forEach(player -> player.teleport(tntSpawn));
        survivorTeam.getPlayers().forEach(player -> player.teleport(survivorSpawn));
    }

    public void clearTeams() {
        teamMapper.apply(GameConfig.TNT_ID).clearPlayers();
        teamMapper.apply(GameConfig.SURVIVOR_ID).clearPlayers();
    }
}
