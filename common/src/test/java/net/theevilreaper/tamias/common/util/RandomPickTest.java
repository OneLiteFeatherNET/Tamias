package net.theevilreaper.tamias.common.util;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RandomPickTest {

    private static final long SEED = 42L;
    private static final int DRAWS = 1000;

    private enum Color { RED, GREEN, BLUE, YELLOW }

    private enum Single { ONLY }

    @Test
    void testNextDifferentNeverRepeatsThePrevious() {
        RandomGenerator random = new Random(SEED);

        for (Color previous : Color.values()) {
            for (int i = 0; i < DRAWS; i++) {
                assertNotEquals(previous, RandomPick.nextDifferent(Color.values(), previous, random),
                        "The constant " + previous + " must not repeat");
            }
        }
    }

    @Test
    void testNextDifferentReachesEveryConstantWithoutPrevious() {
        RandomGenerator random = new Random(SEED);
        Set<Color> drawn = EnumSet.noneOf(Color.class);

        for (int i = 0; i < DRAWS; i++) {
            drawn.add(RandomPick.nextDifferent(Color.values(), null, random));
        }

        assertEquals(EnumSet.allOf(Color.class), drawn, "Every constant must be reachable");
    }

    @Test
    void testNextDifferentReachesEveryOtherConstantAfterAPrevious() {
        RandomGenerator random = new Random(SEED);

        for (Color previous : Color.values()) {
            Set<Color> drawn = EnumSet.noneOf(Color.class);
            for (int i = 0; i < DRAWS; i++) {
                drawn.add(RandomPick.nextDifferent(Color.values(), previous, random));
            }
            assertEquals(EnumSet.complementOf(EnumSet.of(previous)), drawn,
                    "Every other constant must be reachable after " + previous);
        }
    }

    @Test
    void testNextDifferentReturnsTheOnlyConstantAgain() {
        RandomGenerator random = new Random(SEED);

        assertSame(Single.ONLY, RandomPick.nextDifferent(Single.values(), Single.ONLY, random));
    }
}
