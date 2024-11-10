package net.theevilreaper.tamias.game.listener.game;

import net.theevilreaper.tamias.common.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.team.TeamHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class RoundFinishListener implements Consumer<RoundEndEvent> {

    private final TeamHelper teamDistributor;

    public RoundFinishListener(@NotNull TeamHelper teamDistributor) {
        this.teamDistributor = teamDistributor;
    }

    @Override
    public void accept(@NotNull RoundEndEvent event) {
        teamDistributor.clearTeams();
    }
}
