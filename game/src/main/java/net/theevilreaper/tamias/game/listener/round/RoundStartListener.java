package net.theevilreaper.tamias.game.listener.round;

import net.theevilreaper.tamias.game.round.RoundProvider;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import net.theevilreaper.tamias.game.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class RoundStartListener implements Consumer<RoundStartEvent> {

    private final Scoreboard scoreboard;
    private final RoundProvider roundProvider;

    /**
     * Creates a new {@link RoundStartListener} that updates the scoreboard when a round starts.
     *
     * @param scoreboard the scoreboard to update
     * @param roundProvider    the round provider to get the current round information
     */
    public RoundStartListener(@NotNull Scoreboard scoreboard, @NotNull RoundProvider roundProvider) {
        this.scoreboard = scoreboard;
        this.roundProvider = roundProvider;
    }


    @Override
    public void accept(@NotNull RoundStartEvent event) {
        this.scoreboard.updateRound(roundProvider.getCurrentRoundComponent());
    }
}
