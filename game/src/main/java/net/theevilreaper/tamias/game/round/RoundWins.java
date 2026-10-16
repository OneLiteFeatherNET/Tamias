package net.theevilreaper.tamias.game.round;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.player.RoundRole;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.xerus.api.team.Team;

/**
 * The {@link RoundWins} class credits round wins to the players.
 * A round win is credited by the role a player had when the round started, so survivors which are converted to
 * bombers during a round don't share the win of the bombers.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RoundWins {

    /**
     * Stores the current role of each team member as its start role.
     * Must be called after the teams are allocated and before any player can be converted.
     *
     * @param survivorTeam the survivor team
     * @param bomberTeam   the bomber team
     */
    public static void snapshotRoles(Team survivorTeam, Team bomberTeam) {
        setStartRole(survivorTeam, RoundRole.SURVIVOR);
        setStartRole(bomberTeam, RoundRole.BOMBER);
    }

    /**
     * Credits a round win to the players of the winning team which also started the round in that team.
     * Must be called before the teams are cleared.
     *
     * @param survivorTeam the survivor team
     * @param bomberTeam   the bomber team
     */
    public static void credit(Team survivorTeam, Team bomberTeam) {
        Team winner = RoundConditions.getWinner(survivorTeam, bomberTeam);
        RoundRole winnerRole = winner == survivorTeam ? RoundRole.SURVIVOR : RoundRole.BOMBER;
        for (Player player : winner.getPlayers()) {
            if (player instanceof TamiasPlayer tamiasPlayer && tamiasPlayer.getStartRole() == winnerRole) {
                tamiasPlayer.addRoundWin();
            }
        }
    }

    private static void setStartRole(Team team, RoundRole role) {
        for (Player player : team.getPlayers()) {
            if (player instanceof TamiasPlayer tamiasPlayer) {
                tamiasPlayer.setStartRole(role);
            }
        }
    }

    private RoundWins() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
