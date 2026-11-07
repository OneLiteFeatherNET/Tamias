package net.theevilreaper.tamias.game.player;

import net.theevilreaper.tamias.common.config.BomberAssistConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BomberAssistStateTest {

    private static final BomberAssistConfig CONFIG = new BomberAssistConfig(0.25, 0.5, 6, 30);

    @Test
    void testNoNearMissGivesNoBonus() {
        assertEquals(0, new BomberAssistState().bonus(CONFIG, 1000));
    }

    @Test
    void testNearMissesGrowTheBonusUpToTheMax() {
        BomberAssistState state = new BomberAssistState();

        state.recordNearMiss(CONFIG, 1000);
        assertEquals(0.25, state.bonus(CONFIG, 1000));
        state.recordNearMiss(CONFIG, 2000);
        state.recordNearMiss(CONFIG, 3000);
        assertEquals(0.5, state.bonus(CONFIG, 3000));
    }

    @Test
    void testBonusDecaysAfterTheLastNearMiss() {
        BomberAssistState state = new BomberAssistState();
        state.recordNearMiss(CONFIG, 1000);

        assertEquals(0.25, state.bonus(CONFIG, 31_000));
        assertEquals(0, state.bonus(CONFIG, 31_001));
    }

    @Test
    void testNearMissAfterTheDecayStartsOver() {
        BomberAssistState state = new BomberAssistState();
        state.recordNearMiss(CONFIG, 1000);
        state.recordNearMiss(CONFIG, 60_000);

        assertEquals(1, state.nearMisses(CONFIG, 60_000));
    }

    @Test
    void testResetForgetsTheNearMisses() {
        BomberAssistState state = new BomberAssistState();
        state.recordNearMiss(CONFIG, 1000);
        state.reset();

        assertEquals(0, state.bonus(CONFIG, 1000));
    }
}
