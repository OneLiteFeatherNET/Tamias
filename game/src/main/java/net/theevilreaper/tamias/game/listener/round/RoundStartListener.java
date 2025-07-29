package net.theevilreaper.tamias.game.listener.round;

import net.theevilreaper.tamias.common.round.event.RoundStartEvent;
import net.theevilreaper.tamias.game.scoreboard.ScoreType;
import net.theevilreaper.tamias.game.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class RoundStartListener implements Consumer<RoundStartEvent> {

    private final Scoreboard scoreboard;

    /**
     * Constructs a new RoundStartListener.
     *
     * @param scoreboard the scoreboard to update when a round starts
     */
    public RoundStartListener(@NotNull Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public void accept(@NotNull RoundStartEvent event) {
        scoreboard.updateScore(ScoreType.ROUND_TYPE, event.displayComponent());
    }
}
