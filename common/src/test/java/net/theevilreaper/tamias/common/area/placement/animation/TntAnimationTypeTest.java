package net.theevilreaper.tamias.common.area.placement.animation;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class TntAnimationTypeTest {

    private static final long SEED = 42L;
    private static final int DRAWS = 1000;

    @Test
    void testNextNeverRepeatsThePreviousType() {
        RandomGenerator random = new Random(SEED);

        for (TntAnimationType previous : TntAnimationType.values()) {
            for (int i = 0; i < DRAWS; i++) {
                assertNotEquals(previous, TntAnimationType.next(previous, random), "The type " + previous + " must not repeat");
            }
        }
    }

    @Test
    void testNextReachesEveryTypeWithoutPrevious() {
        RandomGenerator random = new Random(SEED);
        Set<TntAnimationType> drawn = EnumSet.noneOf(TntAnimationType.class);

        for (int i = 0; i < DRAWS; i++) {
            drawn.add(TntAnimationType.next(null, random));
        }

        assertEquals(EnumSet.allOf(TntAnimationType.class), drawn, "Every type must be reachable");
    }

    @Test
    void testNextReachesEveryOtherTypeAfterAPrevious() {
        RandomGenerator random = new Random(SEED);

        for (TntAnimationType previous : TntAnimationType.values()) {
            Set<TntAnimationType> drawn = EnumSet.noneOf(TntAnimationType.class);
            for (int i = 0; i < DRAWS; i++) {
                drawn.add(TntAnimationType.next(previous, random));
            }
            assertEquals(EnumSet.complementOf(EnumSet.of(previous)), drawn, "Every other type must be reachable after " + previous);
        }
    }

    @Test
    void testCreateReturnsAFreshInstance() {
        for (TntAnimationType type : TntAnimationType.values()) {
            assertNotSame(type.create(), type.create(), "Every run needs its own animation instance for " + type);
        }
    }

    @Test
    void testCreateReturnsTheAnimationOfTheType() {
        assertInstanceOf(FallingTntAnimation.class, TntAnimationType.FALLING.create());
        assertInstanceOf(GrowingTntAnimation.class, TntAnimationType.GROWING.create());
    }
}
