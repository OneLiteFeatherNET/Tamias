package net.theevilreaper.tamias.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BomberAssistConfigTest {

    @Test
    void testBonusGrowsPerNearMiss() {
        BomberAssistConfig config = new BomberAssistConfig(0.25, 1.0, 6.0, 30);

        assertEquals(0, config.bonus(0));
        assertEquals(0.25, config.bonus(1));
        assertEquals(0.5, config.bonus(2));
    }

    @Test
    void testBonusStopsAtTheMax() {
        assertEquals(0.5, BomberAssistConfig.DEFAULT.bonus(10));
    }

    @Test
    void testZeroMaxBonusTurnsTheAssistOff() {
        BomberAssistConfig config = new BomberAssistConfig(0.25, 0, 6.0, 30);

        assertFalse(config.enabled());
        assertEquals(0, config.bonus(5));
    }

    @Test
    void testDefaultIsEnabled() {
        assertTrue(BomberAssistConfig.DEFAULT.enabled());
    }

    @Test
    void testNegativeValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BomberAssistConfig(-1, 0.5, 6, 30));
        assertThrows(IllegalArgumentException.class, () -> new BomberAssistConfig(0.25, -1, 6, 30));
        assertThrows(IllegalArgumentException.class, () -> new BomberAssistConfig(0.25, 0.5, -1, 30));
        assertThrows(IllegalArgumentException.class, () -> new BomberAssistConfig(0.25, 0.5, 6, -1));
    }
}
