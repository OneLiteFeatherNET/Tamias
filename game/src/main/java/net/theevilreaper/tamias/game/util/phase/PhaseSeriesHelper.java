package net.theevilreaper.tamias.game.util.phase;

import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;

/**
 * The {@link PhaseSeriesHelper} contains operations on phase series which are not provided by Xerus.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PhaseSeriesHelper {

    /**
     * Finishes the given series before all of its phases are played, e.g. to end the game when too few players are
     * left. The active phase is finished as usual, so its cleanup still runs, but the series does not advance to its
     * next phase. Afterward the parent series continues with the phase after the given series.
     *
     * @param series the series to finish
     */
    public static void finishEarly(LinearPhaseSeries<? extends Phase> series) {
        if (series.isFinished()) return;

        Phase activePhase = series.getCurrentPhase();
        // Finishing the active phase advances the series, which is skipped while the series is marked as finished
        series.setFinished(true);
        if (activePhase != null) {
            activePhase.finish();
        }
        series.setFinished(false);
        series.finish();
    }

    private PhaseSeriesHelper() {
        throw new UnsupportedOperationException("The PhaseSeriesHelper class can not be instantiated");
    }
}
