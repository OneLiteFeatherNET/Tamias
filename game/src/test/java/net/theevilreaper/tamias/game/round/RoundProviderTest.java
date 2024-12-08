package net.theevilreaper.tamias.game.round;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoundProviderTest {

    private static final int MAX_ROUNDS = 5;
    private static RoundProvider roundProvider;

    @BeforeAll
    static void setUp() {
        roundProvider = new RoundProvider(MAX_ROUNDS);
    }

    @AfterAll
    static void tearDown() {
        roundProvider = null;
    }

    @Order(1)
    @RepeatedTest(value = 4, name = "Round trigger {currentRepetition} / 5")
    void testNextRound(@NotNull RepetitionInfo repetitionInfo) {
        roundProvider.triggerNextRound();
        assertFalse(roundProvider.isLastRound());
        String roundComponentString = plainText().serialize(roundProvider.getRoundComponent());
        assertNotNull(roundComponentString);
        assertFalse(roundComponentString.isEmpty());
        String currentRound = repetitionInfo.getCurrentRepetition() + " / " + MAX_ROUNDS;
        assertEquals(currentRound, roundComponentString);
    }

    @Order(2)
    @Test
    void testLastRound() {
        roundProvider.triggerNextRound();
        assertFalse(roundProvider.isFirstRound());
        assertTrue(roundProvider.isLastRound());
        String roundComponentString = plainText().serialize(roundProvider.getRoundComponent());
        assertNotNull(roundComponentString);
        assertFalse(roundComponentString.isEmpty());
        assertEquals("5 / " + MAX_ROUNDS, roundComponentString);
    }
}