package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ShootingTntAnimationIntegrationTest {

    private static final Vec CENTER = new Vec(5, 50, 5);
    private static final Vec MEMBER = new Vec(9, 50, 5);
    private static final Vec OTHER = new Vec(9, 50, 5);
    private static final double DELTA = 1e-4;
    private static final int MAX_TICKS = 200;

    @Test
    void testShotSpawnsFallingBlockAtTheLaunchPoint(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(new ShootingTntAnimation());
        tickUntil(env, () -> !findShots(instance).isEmpty(), MAX_TICKS);

        Entity shot = findShots(instance).getFirst();
        // The launch point is the average of the positions, moved to their block centre, and one block above the first group
        assertEquals(7.5, shot.getPosition().x(), DELTA, "The shot must start at the middle of the positions in x");
        assertEquals(5.5, shot.getPosition().z(), DELTA, "The shot must start at the middle of the positions in z");
        assertEquals(51, shot.getPosition().y(), DELTA, "The shot must start one block above the first group");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testNoTntBlockIsPlacedBeforeTheArrival(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new ShootingTntAnimation());
        tickUntil(env, () -> !findShots(instance).isEmpty(), MAX_TICKS);
        tick(env, 5);

        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT block may exist before the shot arrived");
        assertTrue(landed.isEmpty(), "No landing may be reported before the shot arrived");
        assertFalse(findShots(instance).isEmpty(), "The shot must still be flying");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testTntIsPlacedAfterTheArrivalAndTheShotIsRemoved(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new ShootingTntAnimation());
        tickUntil(env, () -> instance.getBlock(CENTER) == Block.TNT, MAX_TICKS);

        assertEquals(Block.TNT, instance.getBlock(CENTER), "The TNT must be placed after the arrival");
        assertEquals(List.of(CENTER), landed, "The landed position must be reported");
        assertTrue(findShots(instance).isEmpty(), "The shot must be removed in the tick the TNT is placed");

        env.destroyInstance(instance);
    }

    @Test
    void testMembersAreLaunchedAfterTheCenter(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(new Vec(6, 50, 5)))), _ -> true);
        placement.place(new ShootingTntAnimation());
        tickUntil(env, () -> instance.getBlock(CENTER) == Block.TNT, MAX_TICKS);

        assertEquals(Block.AIR, instance.getBlock(new Vec(6, 50, 5)), "A member must be launched after the center");
        tickUntil(env, () -> instance.getBlock(new Vec(6, 50, 5)) == Block.TNT, MAX_TICKS);

        assertEquals(List.of(CENTER, new Vec(6, 50, 5)), landed, "The center lands first, then the member");
        env.destroyInstance(instance);
    }

    @Test
    void testPositionWhichIsNotReadyGetsNoShot(@NotNull Env env) {
        Instance instance = createInstance(env);
        Set<Point> readyGround = ConcurrentHashMap.newKeySet();
        readyGround.add(CENTER.sub(0, 1, 0));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(
                new TntGroup(CENTER, List.of()),
                new TntGroup(OTHER, List.of())
        ), readyGround::contains);
        placement.place(new ShootingTntAnimation());
        tickUntil(env, () -> !findShots(instance).isEmpty(), MAX_TICKS);
        tick(env, 5);

        assertEquals(1, findShots(instance).size(), "Only the ready position may get a shot");
        tick(env, 60);
        assertEquals(List.of(CENTER), landed, "Only the ready position may land");
        assertEquals(Block.AIR, instance.getBlock(OTHER), "A position which is not ready must not get TNT");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesTheShotsAndPlacesNothing(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(new ShootingTntAnimation());
        tickUntil(env, () -> !findShots(instance).isEmpty(), MAX_TICKS);

        placement.stop();
        assertTrue(findShots(instance).isEmpty(), "The stop must remove the shots");

        tick(env, 60);
        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT may be placed after the stop");
        assertEquals(Block.AIR, instance.getBlock(MEMBER), "No TNT may be placed after the stop");
        assertTrue(landed.isEmpty(), "No landing may be reported after the stop");
        assertTrue(findShots(instance).isEmpty(), "No shot may be spawned after the stop");

        env.destroyInstance(instance);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    private static List<Entity> findShots(Instance instance) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.FALLING_BLOCK)
                .toList();
    }

    private static void tick(Env env, int ticks) {
        for (int i = 0; i < ticks; i++) {
            env.tick();
        }
    }

    private static void tickUntil(Env env, BooleanSupplier condition, int maxTicks) {
        for (int i = 0; i < maxTicks && !condition.getAsBoolean(); i++) {
            env.tick();
        }
    }
}
