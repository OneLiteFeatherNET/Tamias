package net.theevilreaper.tamias.game.listener.round;

import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.xerus.api.team.Team;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RoundEndListener implements Consumer<RoundEndEvent> {

    private final Supplier<List<Team>> teamClear;

    /**
     * Creates a new {@link RoundEndListener} that shows every team member as a normal player again and clears all teams when a round ends.
     *
     * @param teamClear a supplier that provides the list of teams to clear
     */
    public RoundEndListener(Supplier<List<Team>> teamClear) {
        this.teamClear = teamClear;
    }

    @Override
    public void accept(RoundEndEvent event) {
        List<Team> teams = teamClear.get();

        for (Team team : teams) {
            team.getPlayers().forEach(EntityHelper::switchToPlayer);
            team.clearPlayers();
        }
    }
}
