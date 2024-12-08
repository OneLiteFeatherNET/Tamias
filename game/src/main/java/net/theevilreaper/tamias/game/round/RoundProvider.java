package net.theevilreaper.tamias.game.round;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.common.round.event.RoundStartEvent;
import org.jetbrains.annotations.NotNull;

public final class RoundProvider {

    private final Component maxRoundComponent;
    private final int maxRounds;
    private int currentRound;

    public RoundProvider(int maxRounds) {
        this.maxRounds = maxRounds;
        this.maxRoundComponent = Component.text(" / ", NamedTextColor.GRAY).append(Component.text(maxRounds, NamedTextColor.AQUA));
        this.currentRound = 0;
    }

    /**
     * Triggers the next round if the current round is not the last round.
     */
    public void triggerNextRound() {
        if (currentRound >= maxRounds) return;
        currentRound++;
        Component roundComponent = getRoundComponent();
        EventDispatcher.call(new RoundStartEvent(currentRound, roundComponent));
    }

    @Deprecated
    public @NotNull Component getRoundComponent() {
        return Component.text(currentRound).append(maxRoundComponent);
    }

    /**
     * Checks if the current round is the first round.
     *
     * @return {@code true} if the current round is the first round
     */
    public boolean isFirstRound() {
        return currentRound == 1;
    }

    /**
     * Checks if the current round is the last round.
     *
     * @return {@code true} if the current round is the last round
     */
    public boolean isLastRound() {
        return currentRound == maxRounds;
    }
}
