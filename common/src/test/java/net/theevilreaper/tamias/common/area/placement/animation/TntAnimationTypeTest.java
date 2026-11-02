package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
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
        TntAnimationType previous = null;
        Set<TntAnimationType> drawn = EnumSet.noneOf(TntAnimationType.class);

        for (int i = 0; i < DRAWS; i++) {
            TntAnimationType next = TntAnimationType.next(previous, random);
            assertNotEquals(previous, next, "The type " + previous + " must not repeat");
            drawn.add(next);
            previous = next;
        }

        assertEquals(EnumSet.allOf(TntAnimationType.class), drawn, "Every type must be reachable");
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
        assertInstanceOf(FlyingDeliveryTntAnimation.class, TntAnimationType.FLYING.create());
    }

    @Test
    void testFlyingPicksEveryCarrierType() {
        RandomGenerator random = new Random(SEED);
        Set<EntityType> picked = new HashSet<>();

        for (int i = 0; i < DRAWS; i++) {
            picked.add(FlyingDeliveryTntAnimation.withRandomCarrier(random).carrierType());
        }

        assertEquals(Set.copyOf(FlyingDeliveryTntAnimation.CARRIER_TYPES), picked, "Every animal must be picked at some point");
    }
}
