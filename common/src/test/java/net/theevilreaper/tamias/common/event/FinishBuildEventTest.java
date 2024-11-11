package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinishBuildEventTest {

    @Test
    void testEventCreation() {
        Event finishBuildEvent = new FinishBuildEvent();
        assertInstanceOf(FinishBuildEvent.class, finishBuildEvent);
    }
}
