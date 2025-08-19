package net.theevilreaper.tamias.game.round;

import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.theevilreaper.xerus.api.phase.Phase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundProviderTest {

    private static final int MAX_ROUNDS = 5;
    private static CyclicPhaseSeries<Phase> cyclicPhaseSeries;
    private static RoundProvider roundProvider;

    @BeforeAll
    static void setUp() {
        cyclicPhaseSeries = new CyclicPhaseSeries<>("TestPhaseSeries");
        cyclicPhaseSeries.add(new GamePhase("TestPhase1") {
            @Override
            protected void onStart() {
                // Nothing to do on start
            }
        });
        cyclicPhaseSeries.setMaxIterations(MAX_ROUNDS);
        roundProvider = new RoundProvider(cyclicPhaseSeries);
    }

    @AfterAll
    static void tearDown() {
        assertTrue(cyclicPhaseSeries.isLastPhase());
        assertTrue(cyclicPhaseSeries.isFinished());
        assertTrue(roundProvider.isLastRound());
        roundProvider = null;
    }

    @RepeatedTest(value = 5, name = "Round trigger {currentRepetition} / 5")
    void testNextRound(@NotNull RepetitionInfo repetitionInfo) {
        cyclicPhaseSeries.advance();
        roundProvider.triggerNextRound();
        String roundComponentString = plainText().serialize(roundProvider.getCurrentRoundComponent());
        assertNotNull(roundComponentString);
        assertFalse(roundComponentString.isEmpty());
        String currentRound = repetitionInfo.getCurrentRepetition() + " / " + MAX_ROUNDS;
        assertEquals(currentRound, roundComponentString);
    }
}
