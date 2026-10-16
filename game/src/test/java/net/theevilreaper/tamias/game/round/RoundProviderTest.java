package net.theevilreaper.tamias.game.round;

import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.theevilreaper.xerus.api.phase.Phase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundProviderTest {

    private static final int MAX_ROUNDS = 5;

    private CyclicPhaseSeries<Phase> cyclicPhaseSeries;
    private RoundProvider roundProvider;

    @BeforeEach
    void setUp() {
        cyclicPhaseSeries = new CyclicPhaseSeries<>("TestPhaseSeries");
        cyclicPhaseSeries.add(new GamePhase("TestPhase1") {
            @Override
            protected void onStart() {
                // Nothing to do on start
            }
        });
        // The provider is created before the max iterations are set, like in the game setup
        roundProvider = new RoundProvider(cyclicPhaseSeries);
        cyclicPhaseSeries.setMaxIterations(MAX_ROUNDS);
    }

    @Test
    void testFirstRoundBeforeAnyIteration() {
        assertEquals(1, roundProvider.getCurrentRound());
        assertTrue(roundProvider.isFirstRound());
        assertFalse(roundProvider.isLastRound());
        assertEquals("1 / " + MAX_ROUNDS, plainText().serialize(roundProvider.getCurrentRoundComponent()));
    }

    @Test
    void testRoundsFollowTheCycle() {
        for (int round = 1; round <= MAX_ROUNDS; round++) {
            assertEquals(round, roundProvider.getCurrentRound());
            assertEquals(round == 1, roundProvider.isFirstRound());
            assertEquals(round == MAX_ROUNDS, roundProvider.isLastRound());
            assertEquals(round + " / " + MAX_ROUNDS, plainText().serialize(roundProvider.getCurrentRoundComponent()));
            assertFalse(cyclicPhaseSeries.isFinished());
            cyclicPhaseSeries.advance();
        }

        assertTrue(cyclicPhaseSeries.isFinished());
    }
}
