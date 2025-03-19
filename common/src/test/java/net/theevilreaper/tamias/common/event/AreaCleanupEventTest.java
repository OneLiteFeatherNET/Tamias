package net.theevilreaper.tamias.common.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaCleanupEventTest {

    @Test
    void testAreaCleanupEventCreation() {
        AreaCleanupEvent areaCleanupEvent = new AreaCleanupEvent(true);
        assertTrue(areaCleanupEvent.isSpawn());
    }
}
