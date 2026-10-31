package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ArrowProjectileIntegrationTest {

    private static final double EPSILON = 1e-6;
    private static final double BLOCK_HIT_EPSILON = 1e-4;
    private static final int STUCK_TICKS_BEFORE_REMOVAL = 1200;

    @Test
    void testArrowInAirMovesTheFullLaunchMovementThenDragsAndFalls(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        ArrowProjectile arrow = spawnArrow(env, instance, new Pos(0.5, 60.5, 0.5), new Vec(0.5, 0.2, 0.1));

        env.tick();

        // The first tick moves by the launch movement, drag and gravity only apply to the next movement
        assertPosition(arrow.getPosition(), 1.0, 60.7, 0.6, "after tick 1");
        assertVelocity(arrow, 9.9, 2.96, 1.98, "after tick 1");

        env.tick();

        assertPosition(arrow.getPosition(), 1.495, 60.848, 0.699, "after tick 2");
    }

    @Test
    void testArrowInWaterKeepsSixtyPercentOfTheMovement(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(0, 60, 0, Block.WATER);
        ArrowProjectile arrow = spawnArrow(env, instance, new Pos(0.5, 60.5, 0.5), new Vec(0.5, 0.2, 0.1));

        env.tick();

        assertPosition(arrow.getPosition(), 0.8, 60.62, 0.56, "after one tick in water");
        assertVelocity(arrow, 6.0, 1.4, 1.2, "after one tick in water");
    }

    @Test
    void testArrowSticksJustBeforeTheBlockFace(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(5, 60, 0, Block.STONE);
        ArrowProjectile arrow = spawnArrow(env, instance, new Pos(4.5, 60.5, 0.5), new Vec(1.0, 0.0, 0.0));

        env.tick();

        assertTrue(arrow.isInBlock(), "The arrow should stick in the block");
        assertFalse(arrow.isRemoved(), "A stuck arrow stays in the world");
        assertVelocity(arrow, 0.0, 0.0, 0.0, "after sticking");
        assertBlockHitPosition(arrow.getPosition(), 4.95, 60.5, 0.5, "half a stuck offset before the face");
    }

    @Test
    void testStuckArrowIsRemovedAfterExactly1200Ticks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(5, 60, 0, Block.STONE);
        ArrowProjectile arrow = spawnArrow(env, instance, new Pos(4.5, 60.5, 0.5), new Vec(1.0, 0.0, 0.0));
        env.tick();
        assertTrue(arrow.isInBlock(), "The arrow should be stuck before the countdown starts");

        for (int i = 1; i < STUCK_TICKS_BEFORE_REMOVAL; i++) {
            env.tick();
        }
        assertFalse(arrow.isRemoved(), "The arrow should still be stuck after " + (STUCK_TICKS_BEFORE_REMOVAL - 1) + " ticks");

        env.tick();
        assertTrue(arrow.isRemoved(), "The arrow should be removed after " + STUCK_TICKS_BEFORE_REMOVAL + " stuck ticks");
    }

    @Test
    void testArrowIsRemovedWhenItHitsAPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player target = env.createPlayer(instance, new Pos(5, 60, 0.5));
        ArrowProjectile arrow = spawnArrow(env, instance, new Pos(3.5, 60.5, 0.5), new Vec(1.0, 0.0, 0.0));

        env.tick();
        assertFalse(arrow.isRemoved(), "The arrow should not reach the player during the first tick");

        env.tick();

        assertTrue(arrow.isRemoved(), "The arrow should be removed when it hits " + target.getUsername());
        assertFalse(arrow.isInBlock(), "An entity hit should not leave the arrow stuck");
    }

    private static ArrowProjectile spawnArrow(Env env, Instance instance, Pos position, Vec velocityPerTick) {
        Player shooter = env.createPlayer(instance, new Pos(0, 60, 40));
        ArrowProjectile arrow = new ArrowProjectile(EntityType.ARROW, shooter);
        arrow.setInstance(instance, position).join();
        arrow.setVelocity(velocityPerTick.mul(20));
        return arrow;
    }

    private static void assertPosition(Pos actual, double x, double y, double z, String when) {
        assertEquals(x, actual.x(), EPSILON, "x " + when);
        assertEquals(y, actual.y(), EPSILON, "y " + when);
        assertEquals(z, actual.z(), EPSILON, "z " + when);
    }

    /**
     * Minestom stops a block hit a hair before the face (about 5e-6 blocks), so block hits get a looser tolerance.
     */
    private static void assertBlockHitPosition(Pos actual, double x, double y, double z, String when) {
        assertEquals(x, actual.x(), BLOCK_HIT_EPSILON, "x " + when);
        assertEquals(y, actual.y(), BLOCK_HIT_EPSILON, "y " + when);
        assertEquals(z, actual.z(), BLOCK_HIT_EPSILON, "z " + when);
    }

    private static void assertVelocity(ArrowProjectile arrow, double x, double y, double z, String when) {
        Vec velocity = arrow.getVelocity();
        assertEquals(x, velocity.x(), EPSILON, "velocity x " + when);
        assertEquals(y, velocity.y(), EPSILON, "velocity y " + when);
        assertEquals(z, velocity.z(), EPSILON, "velocity z " + when);
    }
}
