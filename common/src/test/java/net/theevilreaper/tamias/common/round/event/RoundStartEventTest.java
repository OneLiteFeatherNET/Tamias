package net.theevilreaper.tamias.common.round.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
                () -> new RoundStartEvent(roundId, Component.empty()),
                "The round must start with one"
        );
    }

    @Test
    void testRoundStartEventCreation() {
        RoundStartEvent event = new RoundStartEvent(1, Component.text(1));
        assertNotNull(event);
        assertEquals(1, event.round());
        assertEquals("1", PlainTextComponentSerializer.plainText().serialize(event.displayComponent()));
    }
}
