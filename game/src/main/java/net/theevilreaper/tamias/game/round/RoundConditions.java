package net.theevilreaper.tamias.game.round;

import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public final class RoundConditions {

    public static void checkRoundEnd(LinearPhaseSeries<Phase> phaseSeries, TeamService teamService) {
        Phase currentPhase = phaseSeries.getCurrentPhase();
        if (!(currentPhase instanceof PlayingPhase)) return;

        Optional<Team> survivorTeamOpt = teamService.getTeam(GameConfig.SURVIVOR_KEY);
        Optional<Team> bomberTeamOpt = teamService.getTeam(GameConfig.BOMBER_KEY);

        if (survivorTeamOpt.isEmpty() || bomberTeamOpt.isEmpty()) return;

        if (!isRoundOver(survivorTeamOpt.get(), bomberTeamOpt.get())) return;
        currentPhase.finish();
    }

    /**
     * Checks if the round is over. This is the case when no survivor is left or when no bomber is active anymore.
     * A bomber who is a spectator has no TNT left to respawn on and can't change the outcome of the round.
     *
     * @param survivorTeam the team of the survivors
     * @param bomberTeam   the team of the bombers
     * @return true if the round is over
     */
    static boolean isRoundOver(Team survivorTeam, Team bomberTeam) {
        if (survivorTeam.isEmpty()) return true;
        return bomberTeam.getPlayers().stream().allMatch(SpectatorHelper::isSpectator);
    }

    private RoundConditions() {
        // Prevent instantiation
    }
}
