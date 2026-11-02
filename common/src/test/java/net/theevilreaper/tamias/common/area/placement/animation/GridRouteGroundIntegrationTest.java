package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class GridRouteGroundIntegrationTest {

    private static final int Y = 50;

    @Test
    void testRouteTakesTheDetourOverPlacedGround(@NotNull Env env) {
        Instance instance = createInstance(env);
        // The area is a block of 7 x 3 cells, the ground is only placed on the outer rows and the ends
        Set<Vec> area = rectangle(0, 6, 0, 2);
        Set<Vec> ground = new HashSet<>(area);
        for (int x = 1; x <= 5; x++) {
            ground.remove(cell(x, 1));
        }

        List<Vec> route = GridRoute.findRoute(cell(0, 1), cell(6, 1), context(instance, ground, area));

        assertNotNull(route);
        for (Vec step : route) {
            assertTrue(ground.contains(step), "The route must only cross placed ground, but crossed " + step);
        }
        env.destroyInstance(instance);
    }

    @Test
    void testRouteCrossesMissingGroundWhenThereIsNoOtherWay(@NotNull Env env) {
        Instance instance = createInstance(env);
        Set<Vec> area = rectangle(0, 6, 0, 0);
        Set<Vec> ground = Set.of(cell(0, 0), cell(6, 0));

        List<Vec> route = GridRoute.findRoute(cell(0, 0), cell(6, 0), context(instance, ground, area));

        assertNotNull(route, "Without placed ground the route must still be found inside the area");
        assertEquals(6, route.size());
        env.destroyInstance(instance);
    }

    @Test
    void testRouteNeverLeavesTheArea(@NotNull Env env) {
        Instance instance = createInstance(env);
        // A U-shaped area, the straight line between the two arms lies outside of it
        Set<Vec> area = new HashSet<>();
        area.addAll(rectangle(0, 0, 0, 6));
        area.addAll(rectangle(0, 4, 6, 6));
        area.addAll(rectangle(4, 4, 0, 6));

        List<Vec> route = GridRoute.findRoute(cell(0, 0), cell(4, 0), context(instance, area, area));

        assertNotNull(route);
        for (Vec step : route) {
            assertTrue(area.contains(step), "The route must stay inside the area, but left it at " + step);
        }
        env.destroyInstance(instance);
    }

    @Test
    void testNoRouteOutsideTheArea(@NotNull Env env) {
        Instance instance = createInstance(env);
        // Two separate pieces of area, they are only connected over cells outside of it
        Set<Vec> area = new HashSet<>();
        area.addAll(rectangle(0, 1, 0, 0));
        area.addAll(rectangle(5, 6, 0, 0));

        List<Vec> route = GridRoute.findRoute(cell(0, 0), cell(6, 0), context(instance, area, area));

        assertNull(route, "A route which has to leave the area must not be found");
        env.destroyInstance(instance);
    }

    @Test
    void testNearestStandablePrefersPlacedGround(@NotNull Env env) {
        Instance instance = createInstance(env);
        Set<Vec> area = rectangle(0, 6, 0, 0);
        Set<Vec> ground = Set.of(cell(5, 0));

        Vec cell = GridRoute.nearestStandable(cell(1, 0), context(instance, ground, area), _ -> true);

        assertEquals(cell(5, 0), cell, "A cell with placed ground must win over a nearer one without");
        env.destroyInstance(instance);
    }

    @Test
    void testNearestStandableFallsBackToTheArea(@NotNull Env env) {
        Instance instance = createInstance(env);
        Set<Vec> area = Set.of(cell(3, 0));

        Vec cell = GridRoute.nearestStandable(cell(0, 0), context(instance, Set.of(), area), _ -> true);

        assertEquals(cell(3, 0), cell, "Without placed ground the nearest cell inside the area must be used");
        env.destroyInstance(instance);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    private static Vec cell(int x, int z) {
        return new Vec(x, Y, z);
    }

    private static Set<Vec> rectangle(int minX, int maxX, int minZ, int maxZ) {
        Set<Vec> cells = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                cells.add(cell(x, z));
            }
        }
        return cells;
    }

    /**
     * Creates a context in which the given cells have placed ground and the given cells belong to the area.
     */
    private static TntAnimationContext context(Instance instance, Set<Vec> ground, Set<Vec> area) {
        return new TntAnimationContext() {
            @Override
            public Instance instance() {
                return instance;
            }

            @Override
            public boolean hasGround(Vec cell) {
                return ground.contains(cell);
            }

            @Override
            public boolean isInArea(Vec cell) {
                return area.contains(cell);
            }

            @Override
            public void schedule(int delayTicks, Runnable action) {
            }

            @Override
            public void track(Entity entity) {
            }

            @Override
            public void release(Entity entity) {
            }

            @Override
            public void land(Vec position) {
            }
        };
    }
}
