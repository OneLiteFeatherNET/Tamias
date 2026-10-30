package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import net.theevilreaper.tamias.common.area.GameAreaHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.theevilreaper.tamias.BlockAssertions.assertBlock;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TNTPlacementIntegrationTest {

    @Test
    void testTNTPlacement(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Vec start = Vec.ZERO;
        Vec end = new Vec(5, 0, 5);

        instance.loadChunk(start).join();
        instance.loadChunk(end).join();
        instance.setGenerator(unit -> unit.modifier().fill(start, end, Block.STONE));

        List<Vec> positions = new ArrayList<>();

        for (int x = start.blockX(); x <= end.blockX(); x++) {
            for (int z = start.blockZ(); z <= end.blockZ(); z++) {
                Vec pos = new Vec(x, 0, z);
                positions.add(pos);
                instance.setBlock(pos, Block.STONE);
            }
        }
        assertBlock(instance, positions, Block.STONE);

        List<Vec> testBlocks = List.of(start, end);
        TNTPlacement placement = new TNTPlacement(instance, testBlocks);
        assertNotNull(placement);
        assertInstanceOf(AreaBasePlacement.class, placement);

        placement.place();
        assertTrue(placement.isRunning());

        for (int i = 0; i < 20; i++) {
            env.tick();
        }

        assertBlock(instance, testBlocks, Block.TNT);

        // Advance ticks so iterator finishes and placement stops
        for (int i = 0; i < 10 && placement.isRunning(); i++) {
            env.tick();
        }

        assertFalse(placement.isRunning());

        env.destroyInstance(instance);
    }

    @Test
    void testGroupWaitsForTheGroundAndPopsUpItsMembers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Vec center = new Vec(5, 50, 5);
        List<Vec> members = List.of(new Vec(6, 50, 5), new Vec(5, 50, 6));
        Set<Point> placedGround = ConcurrentHashMap.newKeySet();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(center, members)), placedGround::contains);
        placement.place();

        for (int i = 0; i < 20; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty(), "The group must wait until the ground below it is placed");

        // Only a part of the ground is placed, the group still waits
        placedGround.add(center.sub(0, 1, 0));
        placedGround.add(members.getFirst().sub(0, 1, 0));
        for (int i = 0; i < 20; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty(), "The group must wait until the ground below all of its blocks is placed");

        placedGround.add(members.getLast().sub(0, 1, 0));
        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertEquals(center, landed.getFirst(), "The center lands first");
        assertEquals(Set.copyOf(members), Set.copyOf(landed.subList(1, landed.size())));
        assertBlock(instance, List.of(center, members.get(0), members.get(1)), Block.TNT);
        assertFalse(placement.isRunning());

        env.destroyInstance(instance);
    }

    @Test
    void testLandedCompletesOnceEveryTntHasLanded(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Vec center = new Vec(5, 50, 5);
        List<Vec> members = List.of(new Vec(6, 50, 5), new Vec(5, 50, 6));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(center, members)), _ -> true);
        placement.place();
        CompletableFuture<Void> landedFuture = placement.landed();

        // The queue is empty after the first tick, but the group is still falling
        env.tick();
        env.tick();
        assertFalse(landedFuture.isDone());

        env.tickWhile(() -> !landedFuture.isDone(), Duration.ofSeconds(5));

        assertEquals(1 + members.size(), landed.size());
        env.destroyInstance(instance);
    }

    @Test
    void testLandedCompletesWithoutGroups(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(), _ -> true);
        placement.place();

        env.tickWhile(() -> !placement.landed().isDone(), Duration.ofSeconds(5));

        assertTrue(placement.landed().isDone());
        env.destroyInstance(instance);
    }

    @Test
    void testEveryReadyGroupIsDroppedInTheSameTick(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<TntGroup> groups = List.of(
                new TntGroup(new Vec(2, 50, 2), List.of()),
                new TntGroup(new Vec(8, 50, 8), List.of())
        );
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(groups, _ -> true);
        placement.place();

        env.tickWhile(landed::isEmpty, Duration.ofSeconds(5));

        assertEquals(groups.size(), landed.size(), "Both groups are ready, so they must land together");
        env.destroyInstance(instance);
    }

    @Test
    void testStopCancelsTheTntWhichHasNotLanded(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Vec center = new Vec(5, 50, 5);
        Vec member = new Vec(6, 50, 5);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(center, List.of(member))), _ -> true);
        placement.place();

        // The group is dropped, but the center is still falling
        env.tick();
        env.tick();
        placement.stop();

        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertTrue(landed.isEmpty());
        assertEquals(Block.AIR, instance.getBlock(center));
        assertEquals(Block.AIR, instance.getBlock(member));

        env.destroyInstance(instance);
    }

    @Test
    void testStopInsideLandingCancelsTheMembersWhichPopUp(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        Vec center = new Vec(5, 50, 5);
        Vec member = new Vec(6, 50, 5);
        List<Vec> landed = new ArrayList<>();
        TNTPlacement[] placementRef = new TNTPlacement[1];

        // The placement is stopped while the center lands, the member must not pop up afterwards
        TNTPlacement placement = new TNTPlacement(instance, position -> {
            landed.add(position);
            placementRef[0].stop();
        });
        placementRef[0] = placement;
        placement.updateGroups(List.of(new TntGroup(center, List.of(member))), _ -> true);
        placement.place();

        for (int i = 0; i < 30; i++) {
            env.tick();
        }

        assertEquals(List.of(center), landed, "Only the center landed before the stop");
        assertEquals(Block.AIR, instance.getBlock(member), "A member must not pop up after the placement was stopped");
        env.destroyInstance(instance);
    }

    private static final Vec SUPPLY_CENTER = new Vec(5, 50, 5);
    private static final List<Vec> SUPPLY_MEMBERS = List.of(new Vec(7, 50, 5), new Vec(5, 50, 8), new Vec(3, 50, 4));

    private static TntGroup supplyGroup(List<Vec> ring) {
        return new TntGroup(SUPPLY_CENTER, SUPPLY_MEMBERS, TntDropType.SUPPLY, ring);
    }

    @Test
    void testSupplyDropLandsCenterAndMembersAndReportsTheImpact(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        List<Vec> landed = new ArrayList<>();
        AtomicInteger impacts = new AtomicInteger();

        TNTPlacement placement = new TNTPlacement(instance, landed::add, _ -> impacts.incrementAndGet());
        placement.updateGroups(List.of(supplyGroup(List.of())), _ -> true);
        placement.place();
        CompletableFuture<Void> landedFuture = placement.landed();
        env.tickWhile(() -> !landedFuture.isDone(), Duration.ofSeconds(10));

        assertTrue(landedFuture.isDone());
        assertEquals(SUPPLY_CENTER, landed.getFirst(), "The center lands first");
        assertEquals(Set.copyOf(SUPPLY_MEMBERS), Set.copyOf(landed.subList(1, landed.size())));
        List<Vec> all = new ArrayList<>(SUPPLY_MEMBERS);
        all.add(SUPPLY_CENTER);
        assertBlock(instance, all, Block.TNT);
        assertEquals(1, impacts.get());
        assertTrue(instance.getEntities().isEmpty(), "No falling block may be left");

        env.destroyInstance(instance);
    }

    @Test
    void testSupplyDropFallsSlowerThanANormalGroup(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(supplyGroup(List.of())), _ -> true);
        placement.place();

        for (int i = 0; i < 10; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty(), "A supply drop must still be falling after the time of a normal drop");

        for (int i = 0; i < TNTPlacement.SUPPLY_FALL_TICKS; i++) {
            env.tick();
        }
        assertEquals(SUPPLY_CENTER, landed.getFirst());

        env.destroyInstance(instance);
    }

    @Test
    void testSupplyDropWaitsForTheRingGround(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        Vec ringPosition = new Vec(7, 49, 6);
        Set<Point> placedGround = ConcurrentHashMap.newKeySet();
        placedGround.add(SUPPLY_CENTER.sub(0, 1, 0));
        SUPPLY_MEMBERS.forEach(member -> placedGround.add(member.sub(0, 1, 0)));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(supplyGroup(List.of(ringPosition))), placedGround::contains);
        placement.place();

        for (int i = 0; i < 60; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty(), "The supply drop must wait until the ground of its ring is placed");

        placedGround.add(ringPosition);
        CompletableFuture<Void> landedFuture = placement.landed();
        env.tickWhile(() -> !landedFuture.isDone(), Duration.ofSeconds(10));

        assertEquals(1 + SUPPLY_MEMBERS.size(), landed.size());
        env.destroyInstance(instance);
    }

    @Test
    void testStopDuringSupplyDropRemovesEverything(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(supplyGroup(List.of())), _ -> true);
        placement.place();
        CompletableFuture<Void> landedFuture = placement.landed();

        // Stop during the approach
        for (int i = 0; i < 10; i++) {
            env.tick();
        }
        placement.stop();
        for (int i = 0; i < 60; i++) {
            env.tick();
        }
        assertTrue(landed.isEmpty());
        assertEquals(Block.AIR, instance.getBlock(SUPPLY_CENTER));
        assertTrue(instance.getEntities().isEmpty(), "The falling supply drop must be removed");
        assertFalse(landedFuture.isDone());

        // Stop right after the impact, while the members fly
        placement.place();
        for (int i = 0; i < TNTPlacement.SUPPLY_FALL_TICKS + 2 + GameAreaHelper.TNT_POP_DELAY; i++) {
            env.tick();
        }
        placement.stop();
        for (int i = 0; i < 60; i++) {
            env.tick();
        }
        assertEquals(List.of(SUPPLY_CENTER), landed, "Only the center landed before the stop");
        SUPPLY_MEMBERS.forEach(member -> assertEquals(Block.AIR, instance.getBlock(member)));
        assertTrue(instance.getEntities().isEmpty(), "The flying members must be removed");

        env.destroyInstance(instance);
    }
}
