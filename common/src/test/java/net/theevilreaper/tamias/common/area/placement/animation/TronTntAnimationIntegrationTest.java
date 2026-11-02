package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TronTntAnimationIntegrationTest {

    private static final int MAX_TICKS = 2000;
    private static final double Y = 50;
    /**
     * The start cell of the racers, the average of the centre and the member of the default layout.
     */
    private static final Vec START = new Vec(5, Y, 5);
    private static final Vec CENTRE = new Vec(9, Y, 5);
    private static final Vec MEMBER = new Vec(1, Y, 5);

    @Test
    void testRacerStartsAtTheCentreCell(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());
        tickUntil(env, () -> !findRacers(instance).isEmpty(), MAX_TICKS);

        List<Entity> racers = findRacers(instance);
        assertEquals(1, racers.size(), "Only the centre must be launched at once");
        assertEquals(START, GridRoute.cellOf(racers.getFirst().getPosition()), "The racer must start at the centre cell");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testRacerMovesAlongCellLinesOneAxisAtATime(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());

        boolean movedBetweenCells = false;
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity racer : findRacers(instance)) {
                Point position = racer.getPosition();
                assertTrue(isOnCellLine(position.x()) || isOnCellLine(position.z()),
                        "A racer must stay on a cell line, but it was at " + position);
                if (!isOnCellLine(position.x()) || !isOnCellLine(position.z())) {
                    movedBetweenCells = true;
                }
            }
        }

        assertTrue(movedBetweenCells, "The racers must be seen between two cells, otherwise the test checks nothing");
        env.destroyInstance(instance);
    }

    @Test
    void testRacerDrivesAroundAWallWithoutEnteringIt(@NotNull Env env) {
        Instance instance = createInstance(env);
        for (int z = 3; z <= 7; z++) {
            instance.setBlock(new Vec(7, Y, z), Block.STONE);
        }
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());

        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity racer : findRacers(instance)) {
                Vec cell = GridRoute.cellOf(racer.getPosition());
                assertFalse(instance.getBlock(cell).solid(), "A racer must never drive into a solid cell, but it was at " + cell);
            }
        }

        assertTrue(landed.contains(CENTRE), "The racer must drive around the wall and reach its target");
        env.destroyInstance(instance);
    }

    @Test
    void testTrailAppearsBehindTheRacer(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());
        tickUntil(env, () -> !findTrails(instance).isEmpty(), MAX_TICKS);

        assertFalse(findTrails(instance).isEmpty(), "A trail must appear behind the racer");
        assertFalse(findRacers(instance).isEmpty(), "The trail must appear while the racer is still driving");
        env.destroyInstance(instance);
    }

    @Test
    void testTrailVanishesAfterItsLifetime(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertFalse(findTrails(instance).isEmpty(), "The trail must still exist right after the arrival");
        tick(env, TronTntAnimation.TRAIL_LIFETIME + 2);

        assertTrue(findTrails(instance).isEmpty(), "The trail must vanish after its lifetime");
        env.destroyInstance(instance);
    }

    @Test
    void testTntLandsOnArrivalAndTheRacerIsRemoved(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();
        List<Vec> positions = List.of(CENTRE, MEMBER);

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(placement.landed().isDone(), "The run must complete once both racers arrived");
        assertEquals(Set.copyOf(positions), Set.copyOf(landed), "Every position must be reported as landed");
        assertEquals(2, countTnt(instance, positions), "Both positions must hold a TNT block");
        assertTrue(findRacers(instance).isEmpty(), "The racers must be removed on arrival");
        env.destroyInstance(instance);
    }

    @Test
    void testMembersStartLaterThanTheCentre(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> members = List.of(new Vec(1, Y, 5), new Vec(5, Y, 9));

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTRE, members)), _ -> true);
        placement.place(new TronTntAnimation());

        // The first tick in which a racer is seen is recorded per racer, in the order the racers appear
        Map<Integer, Integer> firstSeen = new LinkedHashMap<>();
        for (int tick = 1; tick <= MAX_TICKS && firstSeen.size() < 3; tick++) {
            env.tick();
            for (Entity racer : findRacers(instance)) {
                firstSeen.putIfAbsent(racer.getEntityId(), tick);
            }
        }

        assertEquals(3, firstSeen.size(), "The centre and both members must be launched");
        List<Integer> ticks = new ArrayList<>(firstSeen.values());
        int centreTick = ticks.getFirst();
        assertTrue(ticks.get(1) - centreTick >= TronTntAnimation.LAUNCH_DELAY,
                "The first member must start " + TronTntAnimation.LAUNCH_DELAY + " ticks after the centre");
        assertTrue(ticks.get(2) - centreTick >= 2 * TronTntAnimation.LAUNCH_DELAY,
                "The second member must start " + 2 * TronTntAnimation.LAUNCH_DELAY + " ticks after the centre");
        env.destroyInstance(instance);
    }

    @Test
    void testTargetWithoutRouteLandsAtOnceAndTheRunCompletes(@NotNull Env env) {
        Instance instance = createInstance(env);
        // The centre is enclosed on all four sides, so no route reaches it
        instance.setBlock(new Vec(8, Y, 5), Block.STONE);
        instance.setBlock(new Vec(10, Y, 5), Block.STONE);
        instance.setBlock(new Vec(9, Y, 4), Block.STONE);
        instance.setBlock(new Vec(9, Y, 6), Block.STONE);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());
        int ticks = tickUntil(env, () -> landed.contains(CENTRE), MAX_TICKS);

        assertTrue(ticks <= 2, "A target without a route must land at once, but it took " + ticks + " ticks");
        assertTrue(findRacers(instance).isEmpty(), "No racer may be spawned for a centre without a route");

        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);
        assertTrue(placement.landed().isDone(), "The run must complete although the centre had no route");
        assertEquals(Set.copyOf(List.of(CENTRE, MEMBER)), Set.copyOf(landed), "Both positions must land");
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesRacersAndTrailsAndPlacesNothingAfterwards(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = List.of(CENTRE, MEMBER);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTRE, List.of(MEMBER))), _ -> true);
        placement.place(new TronTntAnimation());
        tickUntil(env, () -> !findRacers(instance).isEmpty(), MAX_TICKS);
        assertFalse(findRacers(instance).isEmpty(), "The racer must still be driving before the stop");

        placement.stop();
        int landedAfterStop = landed.size();
        int tntAfterStop = countTnt(instance, positions);
        assertEquals(0, countBlockDisplays(instance), "The stop must remove the racers and the trails");

        tick(env, 60);

        assertEquals(0, countBlockDisplays(instance), "No racer or trail may be spawned after the stop");
        assertEquals(landedAfterStop, landed.size(), "No landing may be reported after the stop");
        assertEquals(tntAfterStop, countTnt(instance, positions), "The block count must not change after the stop");
        env.destroyInstance(instance);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    /**
     * Checks if a coordinate lies on a cell line, which is a whole block coordinate.
     */
    private static boolean isOnCellLine(double coordinate) {
        return Math.abs(coordinate - Math.rint(coordinate)) < 1e-9;
    }

    private static List<Entity> findRacers(Instance instance) {
        return findBlockDisplays(instance, Block.TNT);
    }

    private static List<Entity> findTrails(Instance instance) {
        return findBlockDisplays(instance, TronTntAnimation.TRAIL_BLOCK);
    }

    private static List<Entity> findBlockDisplays(Instance instance, Block block) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.BLOCK_DISPLAY)
                .filter(entity -> ((BlockDisplayMeta) entity.getEntityMeta()).getBlockStateId().equals(block))
                .toList();
    }

    private static int countBlockDisplays(Instance instance) {
        return (int) instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.BLOCK_DISPLAY)
                .count();
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

    private static void tick(Env env, int ticks) {
        for (int i = 0; i < ticks; i++) {
            env.tick();
        }
    }

    /**
     * Ticks until the condition holds or the limit is reached.
     *
     * @return the number of ticks which were run
     */
    private static int tickUntil(Env env, BooleanSupplier condition, int maxTicks) {
        int ticks = 0;
        while (ticks < maxTicks && !condition.getAsBoolean()) {
            env.tick();
            ticks++;
        }
        return ticks;
    }
}
