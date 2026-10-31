package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class VanillaProjectileTest {

    private static final double EPSILON = 1e-6;
    private static final double UNCERTAINTY_SCALE = 0.0172275;

    @Test
    void testMovementToShootDrawsTwoRandomNumbersPerAxis() {
        // x uses the first pair (0.75 - 0.25), y the second pair (0.5 - 0.5), z the third pair (0.25 - 0.75)
        RandomGenerator random = scripted(0.75, 0.25, 0.5, 0.5, 0.25, 0.75);

        Vec movement = VanillaProjectile.getMovementToShoot(new Vec(0, 0, 4), 1.5, 1.0, random);

        assertEquals(0.75 * UNCERTAINTY_SCALE, movement.x(), EPSILON, "The x deviation should use the first two draws");
        assertEquals(0.0, movement.y(), EPSILON, "The y deviation should use the next two draws");
        assertEquals(1.5 - 0.75 * UNCERTAINTY_SCALE, movement.z(), EPSILON, "The z deviation should use the last two draws");
    }

    @Test
    void testLerpRotationMovesTwentyPercentOfTheWayTowardsTheTarget() {
        assertEquals(20f, VanillaProjectile.lerpRotation(0f, 100f), 1e-4f, "The rotation should move 20% towards the target");
    }

    @Test
    void testLerpRotationTakesTheShortWayAcrossZero() {
        // 350 to 10 is 20 degrees over the wrap, the lerp must not turn 340 degrees the other way
        assertEquals(-6f, VanillaProjectile.lerpRotation(350f, 10f), 1e-4f, "The rotation should cross 0 degrees");
    }

    @Test
    void testLerpRotationTakesTheShortWayBackAcrossZero() {
        assertEquals(366f, VanillaProjectile.lerpRotation(10f, 350f), 1e-4f, "The rotation should cross 0 degrees backwards");
    }

    @ParameterizedTest(name = "tick {0} has margin {1}")
    @CsvSource({"0, 0.0", "2, 0.0", "3, 0.05", "8, 0.3", "20, 0.3"})
    void testHitMarginGrowsFromTheThirdTickUpToThreeTenths(long tick, double expectedMargin) {
        assertEquals(expectedMargin, VanillaProjectile.hitMargin(tick), EPSILON, "The entity hit margin at tick " + tick);
    }

    @Test
    void testShooterIsOutOfReachWhenTheProjectileIsBesideIt() {
        BoundingBox projectileBox = new BoundingBox(0.25, 0.25, 0.25);
        BoundingBox shooterBox = new BoundingBox(0.6, 1.8, 0.6);

        // The projectile box inflated by one block reaches 1.125 blocks from the projectile, the shooter box starts at 1.2
        assertTrue(VanillaProjectile.isClearOfShooter(projectileBox, Vec.ZERO, shooterBox, new Vec(0, 0, -1.5)),
                "A projectile 1.5 blocks away should have left the shooter");
    }

    @Test
    void testShooterIsStillInReachWhileTheProjectileIsCloseToIt() {
        BoundingBox projectileBox = new BoundingBox(0.25, 0.25, 0.25);
        BoundingBox shooterBox = new BoundingBox(0.6, 1.8, 0.6);

        assertFalse(VanillaProjectile.isClearOfShooter(projectileBox, Vec.ZERO, shooterBox, new Vec(0, 0, -1)),
                "A projectile 1 block away is still inside the inflated shooter reach");
    }

    @Test
    void testShooterStaysInReachWhenTheMovementSweepsTowardsIt() {
        BoundingBox projectileBox = new BoundingBox(0.25, 0.25, 0.25);
        BoundingBox shooterBox = new BoundingBox(0.6, 1.8, 0.6);

        assertFalse(VanillaProjectile.isClearOfShooter(projectileBox, new Vec(0, 0, -2), shooterBox, new Vec(0, 0, -3)),
                "The movement towards the shooter should keep it in reach");
    }

    private static RandomGenerator scripted(double... values) {
        return new RandomGenerator() {
            private int index;

            @Override
            public long nextLong() {
                throw new UnsupportedOperationException("Only doubles are scripted");
            }

            @Override
            public double nextDouble() {
                return values[index++];
            }
        };
    }
}
