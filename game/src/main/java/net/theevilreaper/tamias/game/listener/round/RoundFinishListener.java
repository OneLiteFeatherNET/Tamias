package net.theevilreaper.tamias.game.listener.round;

import de.icevizion.xerus.api.team.Team;
import net.theevilreaper.tamias.common.round.event.RoundEndEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RoundFinishListener implements Consumer<RoundEndEvent> {

    private final Supplier<List<Team>> teamClear;

    public RoundFinishListener(@NotNull Supplier<List<Team>> teamClear) {
        this.teamClear = teamClear;
    }

    @Override
    public void accept(@NotNull RoundEndEvent event) {
        List<Team> teams = teamClear.get();

        for (Team team : teams) {
            team.clearPlayers();
        }
    }
}
