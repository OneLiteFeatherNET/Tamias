package net.theevilreaper.tamias.game.round;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;

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

    /**
     * Creates a new {@link RoundProvider} with the given phase series.
     *
     * @param phaseSeries the phase series to get the round information from
     */
    public RoundProvider(CyclicPhaseSeries<Phase> phaseSeries) {
        this.phaseSeries = phaseSeries;
    }

    /**
     * Returns the current round, starting at {@code 1}.
     * The iterations of the {@link CyclicPhaseSeries} start at {@code 0} and are only increased after the last
     * phase of a cycle has finished, so the running round is always one ahead of the iteration count.
     *
     * @return the current round
     */
    public int getCurrentRound() {
        return phaseSeries.getIterations() + 1;
    }

    /**
     * Checks if the current round is the first round.
     *
     * @return {@code true} if the current round is the first round
     */
    public boolean isFirstRound() {
        return getCurrentRound() == 1;
    }

    /**
     * Checks if the current round is the last round.
     *
     * @return {@code true} if the current round is the last round
     */
    public boolean isLastRound() {
        return getCurrentRound() == phaseSeries.getMaxIterations();
    }

    /**
     * Gets the current round component which represents the current round.
     *
     * @return the current round component
     */
    public Component getCurrentRoundComponent() {
        return outOfMaxRounds(getCurrentRound());
    }

    /**
     * Creates a component which shows the given amount of won rounds out of all rounds of the game.
     *
     * @param wins the amount of won rounds
     * @return the component with the won rounds
     */
    public Component getWinsComponent(int wins) {
        return outOfMaxRounds(wins);
    }

    private Component outOfMaxRounds(int value) {
        return Component.text(value)
                .append(Component.text(" / ", NamedTextColor.GRAY))
                .append(Component.text(phaseSeries.getMaxIterations(), NamedTextColor.AQUA));
    }
}
