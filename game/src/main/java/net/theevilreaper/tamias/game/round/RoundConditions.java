package net.theevilreaper.tamias.game.round;

import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
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

        Team survivorTeam = survivorTeamOpt.get();
        Team bomberTeam = bomberTeamOpt.get();

        if (survivorTeam.isEmpty()) {
            currentPhase.finish();
            return;
        }

        if (!bomberTeam.isEmpty()) return;
        currentPhase.finish();
    }

    private RoundConditions() {
        // Prevent instantiation
    }
}
