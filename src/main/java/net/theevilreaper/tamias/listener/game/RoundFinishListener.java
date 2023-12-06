package net.theevilreaper.tamias.listener.game;

import net.theevilreaper.tamias.round.events.RoundFinishEvent;
import net.theevilreaper.tamias.team.TeamHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class RoundFinishListener implements Consumer<RoundFinishEvent> {

    private final TeamHelper teamDistributor;

    public RoundFinishListener(@NotNull TeamHelper teamDistributor) {
        this.teamDistributor = teamDistributor;
    }

    @Override
    public void accept(@NotNull RoundFinishEvent event) {
        teamDistributor.clearTeams();
    }
}
