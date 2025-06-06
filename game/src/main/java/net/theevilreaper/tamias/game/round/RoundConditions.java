package net.theevilreaper.tamias.game.round;

import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class RoundConditions {

    public static void checkRoundEnd(@NotNull LinearPhaseSeries<Phase> phaseSeries, @NotNull TeamService<Team> teamService) {
        Phase currentPhase = phaseSeries.getCurrentPhase();
        if (!(currentPhase instanceof PlayingPhase)) return;

        Team survivorTeam = teamService.getTeams().getFirst();
        Team bomberTeam =teamService.getTeams().getLast();

        if (survivorTeam.isEmpty() || survivorTeam.getPlayers().size() - 1 == 0) {
            currentPhase.finish();
            return;
        }

        if (!bomberTeam.isEmpty()) return;
        currentPhase.finish();
        //TODO: Print winner

    }

    private RoundConditions() {
        // Prevent instantiation
    }
}
