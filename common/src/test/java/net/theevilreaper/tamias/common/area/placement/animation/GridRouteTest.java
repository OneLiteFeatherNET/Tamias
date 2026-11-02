package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class GridRouteTest {

    private static final int Y = 50;

    @Test
    void testStraightRouteStepsCellByCell() {
        List<Vec> route = GridRoute.find(cell(0, 0), cell(3, 0), _ -> true, GridRoute.MAX_ROUTE_NODES);

        assertEquals(List.of(cell(1, 0), cell(2, 0), cell(3, 0)), route, "The route must step cell by cell to the target");
    }

    @Test
    void testRouteGoesAroundAWall() {
        Set<Vec> wall = Set.of(cell(1, -1), cell(1, 0), cell(1, 1));

        List<Vec> route = GridRoute.find(cell(0, 0), cell(2, 0), notIn(wall), GridRoute.MAX_ROUTE_NODES);

        assertNotNull(route, "A route around the wall must exist");
        assertEquals(cell(2, 0), route.getLast(), "The route must end at the target");
        for (Vec step : route) {
            assertFalse(wall.contains(step), "The route must not cross the wall at " + step);
        }
        assertTrue(route.size() > 2, "The route around the wall must be longer than the straight line");
    }

    @Test
    void testTargetInsideABlockedCellIsReachable() {
        Set<Vec> blocked = Set.of(cell(2, 0));

        List<Vec> route = GridRoute.find(cell(0, 0), cell(2, 0), notIn(blocked), GridRoute.MAX_ROUTE_NODES);

        assertEquals(List.of(cell(1, 0), cell(2, 0)), route, "The target counts as reachable even if it is not passable");
    }

    @Test
    void testNoRouteWhenTheTargetIsEnclosed() {
        Set<Vec> blocked = Set.of(cell(1, 0), cell(3, 0), cell(2, 1), cell(2, -1));

        List<Vec> route = GridRoute.find(cell(0, 0), cell(2, 0), notIn(blocked), GridRoute.MAX_ROUTE_NODES);

        assertNull(route, "An enclosed target must have no route");
    }

    @Test
    void testNoRouteWhenTheMaxNodesAreExceeded() {
        List<Vec> route = GridRoute.find(cell(0, 0), cell(3, 0), _ -> true, 1);

        assertNull(route, "A search which visits more cells than allowed must give up");
    }

    @Test
    void testAlreadyAtTheTargetGivesAnEmptyRoute() {
        List<Vec> route = GridRoute.find(cell(4, 0), cell(4, 0), _ -> true, GridRoute.MAX_ROUTE_NODES);

        assertNotNull(route, "Being at the target is not the same as having no route");
        assertTrue(route.isEmpty(), "The route to the current cell must be empty");
    }

    @Test
    void testRouteHasNoDiagonalSteps() {
        List<Vec> route = GridRoute.find(cell(0, 0), cell(3, 2), _ -> true, GridRoute.MAX_ROUTE_NODES);

        assertNotNull(route);
        Vec previous = cell(0, 0);
        for (Vec step : route) {
            double manhattan = Math.abs(step.x() - previous.x()) + Math.abs(step.z() - previous.z());
            assertEquals(1, manhattan, "Every step must move to a direct neighbour, but went from " + previous + " to " + step);
            previous = step;
        }
        assertEquals(cell(3, 2), previous, "The route must end at the target");
    }

    @Test
    void testLongRouteOverAnOpenAreaIsFound() {
        // A breadth-first search would visit more than 3000 cells for this route and give up
        List<Vec> route = GridRoute.find(cell(0, 0), cell(40, 40), _ -> true, GridRoute.MAX_ROUTE_NODES);

        assertNotNull(route, "A route across a large open area must be found");
        assertEquals(80, route.size(), "The route must be as short as the distance");
    }

    @Test
    void testOpenAreaOnlyChecksCellsAlongTheRoute() {
        AtomicInteger checks = new AtomicInteger();

        GridRoute.find(cell(0, 0), cell(40, 40), _ -> checks.incrementAndGet() > 0, GridRoute.MAX_ROUTE_NODES);

        assertTrue(checks.get() < 500, "The search must not spread over the whole area, but checked " + checks.get() + " cells");
    }

    @Test
    void testRouteAroundAWallIsTheShortest() {
        // The wall at x = 1 reaches from z = -2 to z = 3, so the way around it at z = -3 is the shortest
        Set<Vec> wall = Set.of(cell(1, -2), cell(1, -1), cell(1, 0), cell(1, 1), cell(1, 2), cell(1, 3));

        List<Vec> route = GridRoute.find(cell(0, 0), cell(2, 0), notIn(wall), GridRoute.MAX_ROUTE_NODES);

        assertNotNull(route);
        assertEquals(8, route.size(), "Three steps down, two across and three back up");
    }

    @Test
    void testNearestPassablePassableCellIsReturnedUnchanged() {
        Vec start = cell(4, 4);

        Vec result = GridRoute.nearestPassable(start, _ -> true, GridRoute.MAX_SPAWN_RADIUS);

        assertEquals(start, result, "A passable cell must be used as it is");
    }

    @Test
    void testNearestPassableBlockedCellGivesTheNearestFreeCell() {
        Vec start = cell(4, 4);
        Set<Vec> blocked = Set.of(start, cell(5, 4), cell(3, 4), cell(4, 5));

        Vec result = GridRoute.nearestPassable(start, notIn(blocked), GridRoute.MAX_SPAWN_RADIUS);

        assertEquals(cell(4, 3), result, "The nearest free cell must be picked, not a diagonal one");
    }

    @Test
    void testNearestPassableReturnsTheCellWhenNothingIsFreeWithinTheRadius() {
        Vec start = cell(4, 4);

        Vec result = GridRoute.nearestPassable(start, _ -> false, 3);

        assertEquals(start, result, "Without a free cell in the radius the original cell must be returned");
    }

    private static Vec cell(int x, int z) {
        return new Vec(x, Y, z);
    }

    private static Predicate<Vec> notIn(Set<Vec> blocked) {
        return cell -> !blocked.contains(cell);
    }
}
