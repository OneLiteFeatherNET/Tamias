package net.theevilreaper.tamias.game.listener.round;

import net.kyori.adventure.text.Component;
import net.theevilreaper.tamias.common.round.event.RoundStartEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RoundStartListener implements Consumer<RoundStartEvent> {

    private final Consumer<Component> roundScoreUpdater;

    public RoundStartListener(@NotNull Consumer<Component> roundScoreUpdater) {
        this.roundScoreUpdater = roundScoreUpdater;
    }

    @Override
    public void accept(@NotNull RoundStartEvent event) {
        roundScoreUpdater.accept(event.displayComponent());
    }
}
