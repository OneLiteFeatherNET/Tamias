package net.theevilreaper.tamias.game.util;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExplosionRangeTest {

    private static final double RADIUS = 3.0;
    private static final BoundingBox TNT_BOX = new BoundingBox(0.98, 0.98, 0.98);
    private static final BoundingBox PLAYER_BOX = new BoundingBox(0.6, 1.8, 0.6);
    private static final Point CENTER = ExplosionRange.center(Vec.ZERO, TNT_BOX);

    @Test
    void testCenterIsTheMiddleOfTheTnt() {
        assertEquals(new Vec(0, 0.49, 0), CENTER);
    }

    @Test
    void testReachesTheEdgeOfTheHitBox() {
        // The feet are 3.2 blocks away, the side of the hit box only 2.9
        assertTrue(ExplosionRange.reaches(CENTER, RADIUS, new Vec(3.2, 0, 0), PLAYER_BOX));
    }

    @Test
    void testMissesBeyondTheHitBox() {
        assertFalse(ExplosionRange.reaches(CENTER, RADIUS, new Vec(3.4, 0, 0), PLAYER_BOX));
    }

    @Test
    void testHeightDifferenceWithinTheBodyDoesNotShrinkTheRange() {
        // A jumping survivor still has the explosion center between the feet and the head
        assertTrue(ExplosionRange.reaches(CENTER, RADIUS, new Vec(3.2, 0.4, 0), PLAYER_BOX));
    }

    @Test
    void testReachesASurvivorTwoBlocksHigher() {
        // The feet are sqrt(2.5² + 2²) = 3.2 blocks away, the nearest point of the hit box is closer
        assertTrue(ExplosionRange.reaches(CENTER, RADIUS, new Vec(2.5, 2, 0), PLAYER_BOX));
    }

    @Test
    void testRewindAddsTheInterpolationToTheLatency() {
        assertEquals(50 + ExplosionRange.INTERPOLATION_MILLIS, ExplosionRange.rewindMillis(50));
    }

    @Test
    void testRewindIsCapped() {
        assertEquals(ExplosionRange.MAX_REWIND_MILLIS, ExplosionRange.rewindMillis(1000));
    }

    @Test
    void testRewindIsNeverNegative() {
        assertEquals(ExplosionRange.INTERPOLATION_MILLIS, ExplosionRange.rewindMillis(-50));
    }
}
