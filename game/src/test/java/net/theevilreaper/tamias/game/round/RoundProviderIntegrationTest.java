package net.theevilreaper.tamias.game.round;

import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.round.event.RoundStartEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MicrotusExtension.class)
class RoundProviderIntegrationTest {

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
    @RepeatedTest(value = 5, name = "Round trigger {currentRepetition} / 5")
    void testNextRound(@NotNull Env env, @NotNull RepetitionInfo repetitionInfo) {
        FlexibleListener<RoundStartEvent> listen = env.listen(RoundStartEvent.class);
        roundProvider.triggerNextRound();
        listen.followup(event -> {
            String roundComponentString = plainText().serialize(event.displayComponent());
            assertNotNull(roundComponentString);
            assertFalse(roundComponentString.isEmpty());
            String currentRound = repetitionInfo.getCurrentRepetition() + " / " + MAX_ROUNDS;
            assertEquals(currentRound, roundComponentString);
        });
    }

    @Order(2)
    @Test
    void testVerificationOfLastRound() {
        assertTrue(roundProvider.isLastRound());
    }
}
