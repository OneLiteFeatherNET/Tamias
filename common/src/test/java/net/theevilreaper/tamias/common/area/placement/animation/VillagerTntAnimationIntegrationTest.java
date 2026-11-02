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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class VillagerTntAnimationIntegrationTest {

    private static final long SEED = 42L;
    private static final int MAX_TICKS = 2000;
    private static final double Y = 50;

    @Test
    void testSpawnsWorkerCountVillagers(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(row(9)), _ -> true);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tick(env, 2);

        assertEquals(VillagerTntAnimation.WORKER_COUNT, findVillagers(instance).size(), "The run must spawn the worker count");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testSpawnsOneWorkerPerPositionWhenThereAreFewerPositions(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        // The ground stays unready, otherwise the workers finish these close positions and vanish within the two ticks
        placement.updateGroups(singleGroups(row(2)), _ -> false);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tick(env, 2);

        assertEquals(2, findVillagers(instance).size(), "The run must not spawn more workers than positions");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testAllPositionsLandAndTheRunCompletes(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(7);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(mixedGroups(positions), _ -> true);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(placement.landed().isDone(), "The run must complete once every position has landed");
        assertEquals(Set.copyOf(positions), Set.copyOf(landed), "Every position must be reported as landed");
        assertEquals(positions.size(), landed.size(), "No position may land twice");

        env.destroyInstance(instance);
    }

    @Test
    void testNoPositionLandsBeforeItsGroundIsReady(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(3);
        Set<Point> readyGround = ConcurrentHashMap.newKeySet();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), readyGround::contains);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tick(env, 60);

        assertTrue(landed.isEmpty(), "No position may land while its ground is not ready");
        assertEquals(0, countTnt(instance, positions), "No TNT block may be placed while the ground is not ready");

        for (Vec position : positions) {
            readyGround.add(position.sub(0, 1, 0));
        }
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertEquals(positions.size(), landed.size(), "The positions must land once their ground is ready");
        env.destroyInstance(instance);
    }

    @Test
    void testAllWorkersAreRemovedAfterTheLastLanding(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(4);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(findVillagers(instance).isEmpty(), "The workers must be removed after the last landing");
        env.destroyInstance(instance);
    }

    @Test
    void testWorkerLandsWithinReachOfThePosition(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Double> distances = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, position -> distances.add(nearestVillagerDistance(instance, position)));
        placement.updateGroups(singleGroups(row(4)), _ -> true);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertEquals(4, distances.size(), "Every position must be landed by a worker");
        for (double distance : distances) {
            assertTrue(distance <= VillagerTntAnimation.WORKER_REACH + 1e-6,
                    "A worker must land at the position it reached, but it was " + distance + " blocks away");
        }
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesTheWorkersAndPlacesNothingAfterwards(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(6);
        Set<Point> readyGround = ConcurrentHashMap.newKeySet();
        readyGround.add(positions.get(0).sub(0, 1, 0));
        readyGround.add(positions.get(1).sub(0, 1, 0));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), readyGround::contains);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        tickUntil(env, () -> landed.size() == 2, MAX_TICKS);
        assertEquals(2, landed.size(), "Two positions must have landed before the stop");
        assertFalse(findVillagers(instance).isEmpty(), "The workers must still be working before the stop");

        placement.stop();
        int tntAfterStop = countTnt(instance, positions);
        int landedAfterStop = landed.size();
        assertTrue(findVillagers(instance).isEmpty(), "The stop must remove the workers");

        tick(env, 60);

        assertEquals(tntAfterStop, countTnt(instance, positions), "The block count must not change after the stop");
        assertEquals(landedAfterStop, landed.size(), "No landing may be reported after the stop");
        assertTrue(findVillagers(instance).isEmpty(), "No worker may be spawned after the stop");
        env.destroyInstance(instance);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    /**
     * Returns the positions in one row, the rows are spaced by one block so the workers never overlap the positions.
     */
    private static List<Vec> row(int count) {
        List<Vec> positions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            positions.add(new Vec(1 + i, Y, 5));
        }
        return positions;
    }

    private static List<TntGroup> singleGroups(List<Vec> positions) {
        return positions.stream().map(position -> new TntGroup(position, List.of())).toList();
    }

    /**
     * Returns one group with a center and two members, followed by single positions for the rest.
     */
    private static List<TntGroup> mixedGroups(List<Vec> positions) {
        List<TntGroup> groups = new ArrayList<>();
        groups.add(new TntGroup(positions.get(0), List.of(positions.get(1), positions.get(2))));
        for (Vec position : positions.subList(3, positions.size())) {
            groups.add(new TntGroup(position, List.of()));
        }
        return groups;
    }

    private static List<Entity> findVillagers(Instance instance) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.VILLAGER)
                .toList();
    }

    private static int countTnt(Instance instance, List<Vec> positions) {
        int count = 0;
        for (Vec position : positions) {
            if (instance.getBlock(position) == Block.TNT) {
                count++;
            }
        }
        return count;
    }

    private static double nearestVillagerDistance(Instance instance, Point position) {
        double nearest = Double.MAX_VALUE;
        double centerX = position.x() + 0.5;
        double centerZ = position.z() + 0.5;
        for (Entity villager : findVillagers(instance)) {
            double distance = Math.hypot(villager.getPosition().x() - centerX, villager.getPosition().z() - centerZ);
            nearest = Math.min(nearest, distance);
        }
        return nearest;
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
