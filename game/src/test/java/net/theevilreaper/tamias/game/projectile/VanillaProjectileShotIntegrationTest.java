package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Launch values of a projectile: the shooter's own movement is added on top of the scaled view direction.
 * The random source is fixed to 0.5, which gives every deviation draw a zero difference.
 */
class VanillaProjectileShotIntegrationTest extends TamiasPlayerTestBase {

    private static final double EPSILON = 1e-6;
    private static final RandomGenerator NO_DEVIATION = new RandomGenerator() {
        @Override
        public long nextLong() {
            throw new UnsupportedOperationException("Only doubles are used by the launch");
        }

        @Override
        public double nextDouble() {
            return 0.5;
        }
    };

    @Test
    void testLaunchAddsTheShooterMovementWhileAirborne(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Entity shooter = spawnShooter(instance, new Pos(0, 60, 0));
        shooter.setVelocity(new Vec(1, -2, 0.5));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        assertSame(instance, egg.getInstance(), "The projectile should be spawned into the shooter's instance");
        // (0, 0, 1.5) plus the shooter movement (0.05, -0.1, 0.025), the y part counts while airborne
        assertVelocity(egg, 1.0, -2.0, 30.5, "for an airborne shooter");
    }

    @Test
    void testFallVelocityOfAShooterOnTheGroundIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Entity shooter = spawnShooter(instance, new Pos(0, 60, 0));
        for (int i = 0; i < 300 && !shooter.isOnGround(); i++) {
            env.tick();
        }
        assertTrue(shooter.isOnGround(), "The shooter should land before the shot");
        shooter.setVelocity(new Vec(1, -2, 0.5));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        assertVelocity(egg, 1.0, 0.0, 30.5, "for a shooter on the ground");
    }

    @Test
    void testNonFiniteShooterMovementCountsAsZero(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Entity shooter = spawnShooter(instance, new Pos(0, 60, 0));
        shooter.setVelocity(new Vec(Double.NaN, Double.NaN, Double.NaN));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        assertVelocity(egg, 0.0, 0.0, 30.0, "for a shooter with a non-finite velocity");
    }

    @Test
    void testTamiasPlayerKnownMovementIsAddedToTheLaunch(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 60, 0));
        env.tick();
        player.teleport(new Pos(0.5, 60, 0.25)).join();
        env.tick();
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, player);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        // The known movement is per tick, the launch velocity is per second
        assertVelocity(egg, 10.0, 0.0, 35.0, "for the movement the player reported");
    }

    @Test
    void testSpawnYawFollowsTheXAxisLikeVanilla(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        // Yaw 270 looks along +x in the view direction of Pos
        Entity shooter = spawnShooter(instance, new Pos(0, 60, 0, 270, 0));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        // Vanilla: yaw = atan2(movement.x, movement.z), a movement along +x gives 90 degrees
        assertEquals(90f, egg.getPosition().yaw(), 1e-3f, "The spawn yaw should follow vanilla's atan2(x, z)");
    }

    @Test
    void testSpawnPitchIsPositiveWhenLaunchedUpward(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        // Pitch -90 looks straight up
        Entity shooter = spawnShooter(instance, new Pos(0, 60, 0, 0, -90));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        assertTrue(egg.getPosition().pitch() > 45f, "A launch upward should have a positive pitch, was " + egg.getPosition().pitch());
    }

    @Test
    void testCancelledShootEventRemovesTheProjectileWithoutSpawningIt(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Entity shooter = spawnShooter(instance, new Pos(0, 60, 0));
        env.process().eventHandler().addListener(EntityShootEvent.class, event -> event.setCancelled(true));
        int entitiesBefore = instance.getEntities().size();
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);

        egg.shoot(new Pos(0, 60, 0), 1.5, 1.0, NO_DEVIATION);

        assertTrue(egg.isRemoved(), "A cancelled shot should remove the projectile");
        assertEquals(entitiesBefore, instance.getEntities().size(), "A cancelled shot should not spawn the projectile");
    }

    private static Entity spawnShooter(Instance instance, Pos position) {
        Entity shooter = new Entity(EntityType.ARMOR_STAND);
        shooter.setInstance(instance, position).join();
        return shooter;
    }

    private static void assertVelocity(ThrownProjectile egg, double x, double y, double z, String when) {
        Vec velocity = egg.getVelocity();
        assertEquals(x, velocity.x(), EPSILON, "velocity x " + when);
        assertEquals(y, velocity.y(), EPSILON, "velocity y " + when);
        assertEquals(z, velocity.z(), EPSILON, "velocity z " + when);
    }
}
