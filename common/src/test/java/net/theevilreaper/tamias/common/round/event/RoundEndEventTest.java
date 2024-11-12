package net.theevilreaper.tamias.common.round.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoundEndEventTest {

    @Test
    void testInvalidRoundEndCreation() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> new RoundEndEvent((byte)-1),
                "The winner team must be greater than 0"
        );
    }

    @Test
    void testValidRoundEndCreation() {
        RoundEndEvent roundEndEvent = new RoundEndEvent((byte)1);
        assertEquals(1, roundEndEvent.winnerTeam());
    }
}
