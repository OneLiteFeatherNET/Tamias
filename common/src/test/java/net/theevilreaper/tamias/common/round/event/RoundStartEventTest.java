package net.theevilreaper.tamias.common.round.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RoundStartEventTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void testInvalidRoundUsageForStartEvent(int roundId) {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> new RoundStartEvent(roundId),
                "The round must start with one"
        );
    }

    @Test
    void testRoundStartEventCreation() {
        RoundStartEvent event = new RoundStartEvent(1);
        assertNotNull(event);
        assertEquals(1, event.round());
    }

}