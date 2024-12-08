package net.theevilreaper.tamias.game.round;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.round.event.RoundStartEvent;

/**
 * The {@link  RoundProvider} manages the current information about which round is active.
 * It contains also to check if it is the first or last round.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RoundProvider {

    private final Component maxRoundComponent;
    private final int maxRounds;
    private int currentRound;

    /**
     * Creates a new instance of the provider with the given max rounds
     *
     * @param maxRounds the maximum amount of rounds
     */
    public RoundProvider(int maxRounds) {
        Check.argCondition(maxRounds < 1, "The max rounds can't be negative or zero");
        this.maxRounds = maxRounds;
        this.maxRoundComponent = Component.text(" / ", NamedTextColor.GRAY).append(Component.text(maxRounds, NamedTextColor.AQUA));
        this.currentRound = 0;
    }

    /**
     * Triggers the next round if the current round is not the last round.
     */
    public void triggerNextRound() {
        if (currentRound + 1 > maxRounds) return;
        currentRound++;
        Component roundComponent = Component.text(currentRound).append(maxRoundComponent);
        EventDispatcher.call(new RoundStartEvent(currentRound, roundComponent));
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
