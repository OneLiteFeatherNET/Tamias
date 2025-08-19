package net.theevilreaper.tamias.game.round;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link  RoundProvider} manages the current information about which round is active.
 * It contains also to check if it is the first or last round.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RoundProvider {

    private final CyclicPhaseSeries<Phase> phaseSeries;
    private final Component maxRoundComponent;

    private Component roundComponent;

    /**
     * Creates a new {@link RoundProvider} with the given phase series.
     *
     * @param phaseSeries the phase series to get the max rounds from
     * @throws IllegalArgumentException if the phase series is null or has no iterations
     */
    public RoundProvider(@NotNull CyclicPhaseSeries<Phase> phaseSeries) {
        this.phaseSeries = phaseSeries;
        int maxRounds = phaseSeries.getMaxIterations();
        this.maxRoundComponent = Component.text(" / ", NamedTextColor.GRAY).append(Component.text(maxRounds, NamedTextColor.AQUA));
    }

    /**
     * Triggers the next round if the current round is not the last round.
     */
    public void triggerNextRound() {
        int currentRound = phaseSeries.getIterations();
        roundComponent = Component.text(currentRound).append(maxRoundComponent);
    }


    /**
     * Checks if the current round is the first round.
     *
     * @return {@code true} if the current round is the first round
     */
    public boolean isFirstRound() {
        return phaseSeries.getIterations() == 1;
    }

    /**
     * Checks if the current round is the last round.
     *
     * @return {@code true} if the current round is the last round
     */
    public boolean isLastRound() {
        return phaseSeries.getIterations() == phaseSeries.getMaxIterations();
    }

    /**
     * Gets the current round component which represents the current round.
     *
     * @return the current round component
     */
    public @NotNull Component getCurrentRoundComponent() {
        return this.roundComponent;
    }
}
