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
class EndermanTntAnimationIntegrationTest {

    private static final long SEED = 42L;
    private static final int MAX_TICKS = 2000;
    private static final double Y = 50;

    @Test
    void testNonReadyPositionsStayEmpty(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(3);
        Set<Point> readyGround = ConcurrentHashMap.newKeySet();
        readyGround.add(positions.getFirst().sub(0, 1, 0));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), readyGround::contains);
        placement.place(new EndermanTntAnimation(new Random(SEED)));
        tick(env, 60);

        assertEquals(List.of(positions.getFirst()), landed, "Only the position with ready ground may land");
        assertEquals(1, countTnt(instance, positions), "No TNT block may be placed on a position with unready ground");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testEndermanAppearsNearThePositionAndNeverInASolidCell(@NotNull Env env) {
        Instance instance = createInstance(env);
        Vec position = new Vec(5, Y, 5);
        buildWallRing(instance, position);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(List.of(position)), _ -> true);
        placement.place(new EndermanTntAnimation(new Random(SEED)));

        double maxDistance = EndermanTntAnimation.APPEAR_RADIUS * Math.sqrt(2) + 1e-6;
        int observed = 0;
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity enderman : findEndermen(instance)) {
                observed++;
                Point feet = enderman.getPosition();
                Vec cell = GridRoute.cellOf(feet);
                assertFalse(instance.getBlock(cell).solid(), "An enderman must never stand in a solid cell");
                assertFalse(instance.getBlock(cell.add(0, 1, 0)).solid(), "An enderman must never stand in a solid cell");
                double distance = Math.hypot(feet.x() - (position.x() + 0.5), feet.z() - (position.z() + 0.5));
                assertTrue(distance <= maxDistance, "An enderman must appear near its position, but it was " + distance + " blocks away");
            }
        }

        assertTrue(observed > 0, "An enderman must appear while the TNT is placed");
        env.destroyInstance(instance);
    }

    @Test
    void testTntLandsOnlyAfterThePlaceDelay(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(1);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new EndermanTntAnimation(new Random(SEED)));
        tickUntil(env, () -> !findEndermen(instance).isEmpty(), MAX_TICKS);
        int ticksAfterAppearance = tickUntil(env, () -> countTnt(instance, positions) == 1, MAX_TICKS);

        assertTrue(ticksAfterAppearance >= EndermanTntAnimation.PLACE_DELAY,
                "The TNT must land " + EndermanTntAnimation.PLACE_DELAY + " ticks after the appearance, but it took "
                        + ticksAfterAppearance);
        env.destroyInstance(instance);
    }

    @Test
    void testEndermanIsRemovedAfterTheLeaveDelay(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(1);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new EndermanTntAnimation(new Random(SEED)));
        tickUntil(env, () -> countTnt(instance, positions) == 1, MAX_TICKS);
        assertFalse(findEndermen(instance).isEmpty(), "The enderman must still be there when the TNT lands");

        int ticksAfterLanding = tickUntil(env, () -> findEndermen(instance).isEmpty(), MAX_TICKS);

        assertTrue(ticksAfterLanding >= EndermanTntAnimation.LEAVE_DELAY,
                "The enderman must leave " + EndermanTntAnimation.LEAVE_DELAY + " ticks after the landing, but it took "
                        + ticksAfterLanding);
        env.destroyInstance(instance);
    }

    @Test
    void testAllPositionsLandAndTheRunCompletes(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(7);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(mixedGroups(positions), _ -> true);
        placement.place(new EndermanTntAnimation(new Random(SEED)));
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(placement.landed().isDone(), "The run must complete once every position has landed");
        assertEquals(Set.copyOf(positions), Set.copyOf(landed), "Every position must be reported as landed");
        assertEquals(positions.size(), landed.size(), "No position may land twice");
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesTheEndermenAndPlacesNothingAfterwards(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(6);
        Set<Point> readyGround = ConcurrentHashMap.newKeySet();
        readyGround.add(positions.get(0).sub(0, 1, 0));
        readyGround.add(positions.get(1).sub(0, 1, 0));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), readyGround::contains);
        placement.place(new EndermanTntAnimation(new Random(SEED)));
        tickUntil(env, () -> !findEndermen(instance).isEmpty(), MAX_TICKS);
        assertFalse(findEndermen(instance).isEmpty(), "The endermen must still be there before the stop");

        placement.stop();
        int tntAfterStop = countTnt(instance, positions);
        int landedAfterStop = landed.size();
        assertTrue(findEndermen(instance).isEmpty(), "The stop must remove the endermen");

        tick(env, 60);

        assertEquals(tntAfterStop, countTnt(instance, positions), "The block count must not change after the stop");
        assertEquals(landedAfterStop, landed.size(), "No landing may be reported after the stop");
        assertTrue(findEndermen(instance).isEmpty(), "No enderman may be spawned after the stop");
        env.destroyInstance(instance);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    /**
     * Surrounds the position with stone on the ring two cells away, so the endermen can only appear inside or outside
     * of the ring.
     */
    private static void buildWallRing(Instance instance, Vec position) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != 2) continue;
                instance.setBlock(position.add(dx, 0, dz), Block.STONE);
            }
        }
    }

    /**
     * Returns the positions in one row, the rows are spaced by one block so the endermen never overlap the positions.
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

    private static List<Entity> findEndermen(Instance instance) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.ENDERMAN)
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
