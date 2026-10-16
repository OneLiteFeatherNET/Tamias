package net.theevilreaper.tamias.game.listener.round;

import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.team.Team;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RoundEndListener implements Consumer<RoundEndEvent> {

    private final Supplier<List<Team>> teamClear;
    private final VoidConsumer staminaCleanup;

    /**
     * Creates a new {@link RoundEndListener} that stops the stamina bars, resets spectators, shows every team member as a normal player
     * again and clears all teams when a round ends.
     *
     * @param teamClear      a supplier that provides the list of teams to clear
     * @param staminaCleanup stops and removes the stamina bars of the round, so they don't keep running in the next one
     */
    public RoundEndListener(Supplier<List<Team>> teamClear, VoidConsumer staminaCleanup) {
        this.teamClear = teamClear;
        this.staminaCleanup = staminaCleanup;
    }

    @Override
    public void accept(RoundEndEvent event) {
        this.staminaCleanup.apply();
        List<Team> teams = teamClear.get();

        for (Team team : teams) {
            team.getPlayers().forEach(player -> {
                SpectatorHelper.disable(player);
                EntityHelper.switchToPlayer(player);
            });
            team.clearPlayers();
        }
    }
}
