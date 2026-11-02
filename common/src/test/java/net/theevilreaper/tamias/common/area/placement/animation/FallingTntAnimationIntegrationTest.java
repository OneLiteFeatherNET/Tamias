package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.placement.TNTPlacement;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class FallingTntAnimationIntegrationTest {

    private static final Vec CENTER = new Vec(5, 50, 5);
    private static final Vec MEMBER = new Vec(6, 50, 5);
    private static final double DELTA = 1e-4;

    @Test
    void testFallingEntitySpawnsCenteredOnTheBlock(@NotNull Env env) {
        Instance instance = createInstance(env, true);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new FallingTntAnimation());
        env.tickWhile(() -> findFalling(instance).isEmpty(), Duration.ofSeconds(5));

        Entity falling = findFalling(instance).getFirst();
        assertNear(CENTER.x() + 0.5, falling.getPosition().x(), "The falling block is rendered around its position, so x must be centered");
        assertNear(CENTER.z() + 0.5, falling.getPosition().z(), "The falling block is rendered around its position, so z must be centered");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testBlockIsNotPlacedAfterFiveTicksAndTheEntityIsStillFalling(@NotNull Env env) {
        Instance instance = createInstance(env, true);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new FallingTntAnimation());
        env.tickWhile(() -> findFalling(instance).isEmpty(), Duration.ofSeconds(5));
        double startY = findFalling(instance).getFirst().getPosition().y();

        for (int i = 0; i < 5; i++) {
            env.tick();
        }

        assertEquals(Block.AIR, instance.getBlock(CENTER), "The TNT must not land after five ticks, the entity is still falling");
        List<Entity> falling = findFalling(instance);
        assertFalse(falling.isEmpty(), "The entity must still be falling after five ticks");
        assertTrue(falling.getFirst().getPosition().y() < startY, "The entity must have moved down");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testTntIsPlacedWhenTheEntityReachedTheGroundAndTheEntityIsRemovedInTheSameTick(@NotNull Env env) {
        Instance instance = createInstance(env, true);

        List<Vec> landed = new ArrayList<>();
        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new FallingTntAnimation());

        double lastFallingY = Double.NaN;
        for (int i = 0; i < 60 && instance.getBlock(CENTER) != Block.TNT; i++) {
            env.tick();
            if (!findFalling(instance).isEmpty()) {
                lastFallingY = findFalling(instance).getFirst().getPosition().y();
            }
        }

        assertEquals(Block.TNT, instance.getBlock(CENTER), "The TNT must be placed once the entity reached the ground");
        assertTrue(findFalling(instance).isEmpty(), "The falling entity must be removed in the tick the TNT is placed");
        assertEquals(List.of(CENTER), landed, "The landed position must be reported");
        assertEquals(CENTER.y(), lastFallingY, 0.01, "The entity must have stood on the ground before the TNT was placed");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testFallOverAPositionWithoutGroundLandsWithinTheSafetyLimit(@NotNull Env env) {
        Instance instance = createInstance(env, false);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new FallingTntAnimation());

        for (int i = 0; i < FallingTntAnimation.MAX_FALL_TICKS && instance.getBlock(CENTER) != Block.TNT; i++) {
            env.tick();
        }

        assertEquals(Block.TNT, instance.getBlock(CENTER), "A fall without collidable ground must still land within the safety limit");
        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testEntityWhichNeverArrivesLandsAtTheSafetyLimit(@NotNull Env env) {
        Instance instance = createInstance(env, false);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new FallingTntAnimation());
        env.tickWhile(() -> findFalling(instance).isEmpty(), Duration.ofSeconds(5));
        // Without gravity the entity hangs in the air and never arrives
        findFalling(instance).getFirst().setNoGravity(true);

        for (int i = 0; i < FallingTntAnimation.MAX_FALL_TICKS - 5; i++) {
            env.tick();
        }
        assertEquals(Block.AIR, instance.getBlock(CENTER), "The TNT must wait for the safety limit");

        for (int i = 0; i < 10 && instance.getBlock(CENTER) != Block.TNT; i++) {
            env.tick();
        }
        assertEquals(Block.TNT, instance.getBlock(CENTER), "The safety limit must land the TNT");
        assertTrue(findFalling(instance).isEmpty(), "The falling entity must be removed at the safety limit");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesTheFallingEntityAndPlacesNothing(@NotNull Env env) {
        Instance instance = createInstance(env, true);

        List<Vec> landed = new ArrayList<>();
        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(new FallingTntAnimation());
        env.tickWhile(() -> findFalling(instance).isEmpty(), Duration.ofSeconds(5));
        for (int i = 0; i < 3; i++) {
            env.tick();
        }

        placement.stop();
        assertTrue(findFalling(instance).isEmpty(), "The falling entity must be removed when the placement stops");

        for (int i = 0; i < 60; i++) {
            env.tick();
        }
        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT may be placed after the stop");
        assertEquals(Block.AIR, instance.getBlock(MEMBER), "No TNT may be placed after the stop");
        assertTrue(landed.isEmpty(), "No landing may be reported after the stop");

        env.destroyInstance(instance);
    }

    /**
     * Creates a flat instance with a loaded chunk. The block below the center is stone, so the falling entity has
     * ground to land on, or the ground is left out to test a fall without collidable ground.
     */
    private static Instance createInstance(Env env, boolean withGround) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        if (withGround) {
            instance.setBlock(CENTER.sub(0, 1, 0), Block.STONE);
            instance.setBlock(MEMBER.sub(0, 1, 0), Block.STONE);
        }
        return instance;
    }

    private static List<Entity> findFalling(Instance instance) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.FALLING_BLOCK)
                .toList();
    }

    private static void assertNear(double expected, double actual, String message) {
        assertEquals(expected, actual, DELTA, message);
    }
}
