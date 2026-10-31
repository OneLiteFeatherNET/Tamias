package net.theevilreaper.tamias.game.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.game.listener.game.ProjectileEntityListener;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ThrownProjectileIntegrationTest extends TamiasPlayerTestBase {

    private static final double EPSILON = 1e-6;
    private static final double BLOCK_HIT_EPSILON = 1e-4;
    private static final byte HIT_STATUS = 3;

    @Test
    void testEggInAirFollowsGravityAndInertiaPerTick(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(0.5, 60.5, 0.5), new Vec(0.5, 0.2, 0.1));

        env.tick();
        assertPosition(egg.getPosition(), 0.995, 60.6683, 0.599, "after tick 1");

        env.tick();
        assertPosition(egg.getPosition(), 1.48505, 60.805217, 0.69701, "after tick 2");

        env.tick();
        assertPosition(egg.getPosition(), 1.9701995, 60.91106483, 0.7940399, "after tick 3");
    }

    @Test
    void testEggInWaterUsesTheWaterInertia(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(0, 60, 0, Block.WATER);
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(0.5, 60.5, 0.5), new Vec(0.5, 0.2, 0.1));

        env.tick();

        // The movement is (0.5, 0.17, 0.1) after gravity, scaled by 0.8 in water instead of 0.99
        assertPosition(egg.getPosition(), 0.9, 60.636, 0.58, "after one tick in water");
    }

    @Test
    void testEggStopsAtTheWallSurfaceWithoutSliding(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(5, 60, 0, Block.STONE);
        TestConnection connection = env.createConnection();
        Player shooter = connection.connect(instance, new Pos(0, 60, 40));
        Collector<EntityStatusPacket> statusTracker = connection.trackIncoming(EntityStatusPacket.class);
        List<ProjectileCollideWithBlockEvent> blockEvents = new ArrayList<>();
        env.process().eventHandler().addListener(ProjectileCollideWithBlockEvent.class, blockEvents::add);

        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);
        egg.setNoGravity(true);
        egg.setInstance(instance, new Pos(4.5, 60.5, 0.5)).join();
        egg.setVelocity(new Vec(1.0, 0.25, 0.25).mul(20));

        env.tick();

        // The movement after inertia is (0.99, 0.2475, 0.2475), the wall at x = 5 is reached after 0.5 / 0.99 of it,
        // so y and z keep the ray's values. Minestom stops a hair (about 5e-6 blocks) before the face
        assertEquals(5.0, egg.getPosition().x(), BLOCK_HIT_EPSILON, "x at the wall surface");
        assertEquals(60.625, egg.getPosition().y(), BLOCK_HIT_EPSILON, "y at the wall surface");
        assertEquals(0.625, egg.getPosition().z(), BLOCK_HIT_EPSILON, "z at the wall surface");
        assertTrue(egg.isRemoved(), "The egg should remove itself at the wall");
        assertEquals(1, blockEvents.size(), "The block collision should be reported once");
        assertEquals(Block.STONE, blockEvents.getFirst().getBlock(), "The event should carry the hit block");
        statusTracker.assertSingle(packet -> {
            assertEquals(egg.getEntityId(), packet.entityId(), "The status should belong to the egg");
            assertEquals(HIT_STATUS, packet.status(), "The hit status should be sent to viewers");
        });
    }

    @Test
    void testEggPassesBesideABlockCorner(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(5, 60, 0, Block.STONE);
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(3.5, 60.5, 1.2), new Vec(1.0, 0.0, 0.0));
        egg.setNoGravity(true);

        for (int i = 0; i < 3; i++) {
            env.tick();
        }

        assertFalse(egg.isRemoved(), "A ray 0.2 blocks beside the corner should not hit the block");
        assertTrue(egg.getPosition().x() > 5.0, "The egg should fly past the block, was at " + egg.getPosition());
    }

    @Test
    void testShooterIsNotHitOnLaunch(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player shooter = env.createPlayer(instance, new Pos(0, 60, 0));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);
        egg.setNoGravity(true);
        egg.setInstance(instance, new Pos(0, 61, 0)).join();

        env.tick();

        assertFalse(egg.isRemoved(), "The egg starts inside its shooter and must not hit it");
    }

    @Test
    void testShooterIsHitWhenTheEggComesBack(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player shooter = env.createPlayer(instance, new Pos(0, 60, 0));
        List<Entity> hitTargets = new ArrayList<>();
        env.process().eventHandler().addListener(ProjectileCollideWithEntityEvent.class, event -> hitTargets.add(event.getTarget()));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);
        egg.setNoGravity(true);
        egg.setInstance(instance, new Pos(0, 61, 0)).join();

        egg.setVelocity(new Vec(0, 0, 1).mul(20));
        for (int i = 0; i < 3; i++) {
            env.tick();
        }
        assertFalse(egg.isRemoved(), "The egg should fly away from its shooter");

        egg.setVelocity(new Vec(0, 0, -1).mul(20));
        for (int i = 0; i < 10 && !egg.isRemoved(); i++) {
            env.tick();
        }

        assertTrue(egg.isRemoved(), "The egg should hit the shooter once it comes back");
        assertEquals(List.of(shooter), hitTargets, "The shooter should be the only hit target");
    }

    @Test
    void testItemEntityInThePathIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        ItemEntity item = new ItemEntity(ItemStack.of(Material.STONE));
        item.setNoGravity(true);
        item.setInstance(instance, new Pos(5.0, 60.5, 0.5)).join();
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(3.5, 60.5, 0.5), new Vec(1.0, 0.0, 0.0));
        egg.setNoGravity(true);

        env.tick();
        env.tick();

        assertFalse(egg.isRemoved(), "Item entities are never hit");
        assertTrue(egg.getPosition().x() > 5.0, "The egg should fly on through the item");
    }

    @Test
    void testOtherProjectileInThePathIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        ThrownProjectile target = spawnEgg(env, instance, new Pos(5.0, 60.5, 0.5), Vec.ZERO);
        target.setNoGravity(true);
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(3.5, 60.5, 0.5), new Vec(1.0, 0.0, 0.0));
        egg.setNoGravity(true);

        env.tick();
        env.tick();

        assertFalse(egg.isRemoved(), "Other projectiles are never hit");
        assertFalse(target.isRemoved(), "The projectile in the path should not be hit either");
    }

    @Test
    void testCancelledEntityEventLetsTheEggFlyOn(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player spectator = env.createPlayer(instance, new Pos(5.0, 60, 0.5));
        SpectatorHelper.enable(spectator);
        ProjectileEntityListener listener = new ProjectileEntityListener(_ -> null);
        AtomicInteger hitEvents = new AtomicInteger();
        env.process().eventHandler().addListener(ProjectileCollideWithEntityEvent.class, event -> {
            hitEvents.incrementAndGet();
            listener.accept(event);
        });
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(3.5, 60.5, 0.5), new Vec(1.0, 0.0, 0.0));
        egg.setNoGravity(true);

        for (int i = 0; i < 3; i++) {
            env.tick();
        }

        // The hidden spectator is only 0.98 blocks wide, the egg is still inside it on the third tick and is offered again
        assertTrue(hitEvents.get() >= 1, "The spectator should have been offered as a hit");
        assertFalse(egg.isRemoved(), "A cancelled hit should let the egg fly on");
        assertTrue(egg.getPosition().x() > 5.5, "The egg should pass the spectator, was at " + egg.getPosition());
    }

    @Test
    void testEntityInFrontOfTheWallIsHitInTheSameTickAsTheWall(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setBlock(5, 60, 0, Block.STONE);
        Player shooter = env.createPlayer(instance, new Pos(0, 60, 40));
        // A small mob between the egg and the wall, in the second half of the sweep up to the wall
        LivingEntity target = new LivingEntity(EntityType.SILVERFISH);
        target.setNoGravity(true);
        target.setInstance(instance, new Pos(4.75, 60.25, 0.5)).join();
        List<Entity> hitTargets = new ArrayList<>();
        List<ProjectileCollideWithBlockEvent> blockEvents = new ArrayList<>();
        env.process().eventHandler().addListener(ProjectileCollideWithEntityEvent.class, event -> hitTargets.add(event.getTarget()));
        env.process().eventHandler().addListener(ProjectileCollideWithBlockEvent.class, blockEvents::add);
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);
        egg.setNoGravity(true);
        egg.setInstance(instance, new Pos(4.0, 60.5, 0.5)).join();
        // After inertia the movement is 1.98 blocks, the wall is reached after the first 1.0 block
        egg.setVelocity(new Vec(2.0, 0.0, 0.0).mul(20));

        env.tick();

        assertTrue(egg.isRemoved(), "The egg should be removed in the tick it reaches the mob");
        assertEquals(List.of(target), hitTargets, "The mob in front of the wall should be hit before the wall");
        assertEquals(0, blockEvents.size(), "The mob stops the egg, the wall is not reached");
    }

    @Test
    void testEggIsRemovedBelowTheWorld(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(0.5, -120, 0.5), new Vec(0, -5, 0));
        egg.setNoGravity(true);

        env.tick();
        assertFalse(egg.isRemoved(), "The egg is still above the removal limit after one tick");

        env.tick();
        assertTrue(egg.isRemoved(), "The egg should be removed once it falls below the world");
    }

    @Test
    void testEggIsRemovedWhenItFliesIntoAnUnloadedChunk(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        List<ProjectileCollideWithBlockEvent> blockEvents = new ArrayList<>();
        env.process().eventHandler().addListener(ProjectileCollideWithBlockEvent.class, blockEvents::add);
        ThrownProjectile egg = spawnEgg(env, instance, new Pos(0.5, 60.5, 0.5), new Vec(0, 0, 1000));
        egg.setNoGravity(true);

        env.tick();

        assertEquals(0, blockEvents.size(), "Unloaded chunks are not blocks, no block collision should be reported");
        assertTrue(egg.isRemoved(), "The egg should be removed on entering a chunk that is not loaded");
    }

    private static ThrownProjectile spawnEgg(Env env, Instance instance, Pos position, Vec velocityPerTick) {
        Player shooter = env.createPlayer(instance, new Pos(0, 60, 40));
        ThrownProjectile egg = new ThrownProjectile(EntityType.EGG, shooter);
        egg.setInstance(instance, position).join();
        egg.setVelocity(velocityPerTick.mul(20));
        return egg;
    }

    private static void assertPosition(Pos actual, double x, double y, double z, String when) {
        assertEquals(x, actual.x(), EPSILON, "x " + when);
        assertEquals(y, actual.y(), EPSILON, "y " + when);
        assertEquals(z, actual.z(), EPSILON, "z " + when);
    }
}
