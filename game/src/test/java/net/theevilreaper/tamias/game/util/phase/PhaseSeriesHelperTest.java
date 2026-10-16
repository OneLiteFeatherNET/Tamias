package net.theevilreaper.tamias.game.util.phase;

import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhaseSeriesHelperTest {

    @Test
    void testFinishEarlySkipsTheRemainingRoundsAndContinuesWithTheNextPhase() {
        CountingPhase roundStart = new CountingPhase("roundStart");
        CountingPhase roundEnd = new CountingPhase("roundEnd");
        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        gameSeries.add(roundStart);
        gameSeries.add(roundEnd);
        gameSeries.setMaxIterations(5);
        CountingPhase restart = new CountingPhase("restart");
        LinearPhaseSeries<Phase> series = new LinearPhaseSeries<>("series");
        series.add(gameSeries);
        series.add(restart);
        series.start();
        assertSame(roundStart, series.getCurrentPhase());

        PhaseSeriesHelper.finishEarly(gameSeries);

        assertTrue(roundStart.isFinished());
        assertFalse(roundEnd.isRunning(), "The next phase of the round must not start");
        assertEquals(0, roundEnd.starts.get());
        assertTrue(gameSeries.isFinished());
        assertSame(restart, series.getCurrentPhase());
        assertTrue(restart.isRunning());
    }

    @Test
    void testFinishEarlyIsIgnoredForAFinishedSeries() {
        CountingPhase roundStart = new CountingPhase("roundStart");
        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        gameSeries.add(roundStart);
        CountingPhase restart = new CountingPhase("restart");
        LinearPhaseSeries<Phase> series = new LinearPhaseSeries<>("series");
        series.add(gameSeries);
        series.add(restart);
        series.start();

        PhaseSeriesHelper.finishEarly(gameSeries);
        PhaseSeriesHelper.finishEarly(gameSeries);

        assertEquals(1, restart.starts.get());
    }

    private static final class CountingPhase extends Phase {

        private final AtomicInteger starts = new AtomicInteger();

        private CountingPhase(@NotNull String name) {
            super(name);
        }

        @Override
        protected void onStart() {
            this.starts.incrementAndGet();
        }
    }
}
