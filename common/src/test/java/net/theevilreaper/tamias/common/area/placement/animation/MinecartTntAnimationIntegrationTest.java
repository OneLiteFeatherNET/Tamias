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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class MinecartTntAnimationIntegrationTest {

    private static final int MAX_TICKS = 4000;

    @Test
    void testTwentyFivePositionsSpawnThreeTntMinecarts(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(twentyFivePositions()), _ -> true);
        placement.place(new MinecartTntAnimation());

        assertEquals(3, findEntities(instance, EntityType.TNT_MINECART).size(), "25 positions must use three carts");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testAllPositionsLandExactlyOnceAndTheFutureCompletes(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = twentyFivePositions();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new MinecartTntAnimation());
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(placement.landed().isDone(), "The run must complete once every position has landed");
        assertEquals(Set.copyOf(positions), Set.copyOf(landed), "Every position must be landed");
        assertEquals(positions.size(), landed.size(), "No position may land twice");
        env.destroyInstance(instance);
    }

    @Test
    void testAllCartsAreRemovedAtTheEnd(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(twentyFivePositions()), _ -> true);
        placement.place(new MinecartTntAnimation());
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(findEntities(instance, EntityType.TNT_MINECART).isEmpty(), "Every cart must be removed at the end");
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesAllCartsAndRailsAndPlacesNothing(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = twentyFivePositions();
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new MinecartTntAnimation());
        tickUntil(env, () -> !findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(), MAX_TICKS);
        tick(env, 5);

        placement.stop();
        assertTrue(findEntities(instance, EntityType.TNT_MINECART).isEmpty(), "The stop must remove every cart");
        assertTrue(findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(), "The stop must remove every rail");

        int tntAfterStop = countTnt(instance, positions);
        int landedAfterStop = landed.size();
        tick(env, 60);

        assertEquals(tntAfterStop, countTnt(instance, positions), "The block count must not change after the stop");
        assertEquals(landedAfterStop, landed.size(), "No landing may be reported after the stop");
        env.destroyInstance(instance);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    /**
     * Returns 25 positions in two rows, twelve on the first row and thirteen on the second.
     */
    private static List<Vec> twentyFivePositions() {
        List<Vec> positions = new ArrayList<>();
        for (int x = 1; x <= 12; x++) {
            positions.add(new Vec(x, 50, 2));
        }
        for (int x = 1; x <= 13; x++) {
            positions.add(new Vec(x, 50, 8));
        }
        return positions;
    }

    private static List<TntGroup> singleGroups(List<Vec> positions) {
        return positions.stream().map(position -> new TntGroup(position, List.of())).toList();
    }

    private static List<Entity> findEntities(Instance instance, EntityType type) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == type)
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

    private static void tickUntil(Env env, BooleanSupplier condition, int maxTicks) {
        for (int i = 0; i < maxTicks && !condition.getAsBoolean(); i++) {
            env.tick();
        }
    }
}
