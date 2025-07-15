package net.theevilreaper.tamias.common.event;

import net.minestom.server.event.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaFinishBuildEventTest {

    @Test
    void testEventCreation() {
        Event finishBuildEvent = new AreaFinishBuildEvent();
        assertInstanceOf(AreaFinishBuildEvent.class, finishBuildEvent);
    }
}
