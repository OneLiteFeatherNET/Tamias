package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Finds routes over block cells for the animations which walk or drive. The routes move in four directions only, so
 * they never cut a corner, and they stay on the height of the start cell.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
final class GridRoute {

    /**
     * The maximum number of cells a route search may visit before it gives up.
     */
    static final int MAX_ROUTE_NODES = 2000;
    /**
     * The largest ring around the average position which is searched for a passable start cell.
     */
    static final int MAX_SPAWN_RADIUS = 32;

    private GridRoute() {
    }

    /**
     * Finds the shortest route of cells from the start to the target with a breadth-first search.
     * The start cell itself is not part of the route, the target is reachable even when it is not passable.
     *
     * @param from     the cell to start at
     * @param to       the cell to reach, only its x and z are used, the route stays on the y of {@code from}
     * @param passable checks if a cell can be walked on
     * @param maxNodes the maximum number of cells the search may visit
     * @return the cells from the first step to the target, an empty list if the start already is the target,
     * or null if there is no route or the search visited more than {@code maxNodes} cells
     */
    static @Nullable List<Vec> find(Vec from, Vec to, Predicate<Vec> passable, int maxNodes) {
        Vec start = cell(from.x(), from.y(), from.z());
        Vec goal = cell(to.x(), start.y(), to.z());
        if (start.x() == goal.x() && start.z() == goal.z()) return List.of();

        Map<Vec, Vec> parents = new HashMap<>();
        parents.put(start, start);
        Deque<Vec> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Vec current = queue.poll();
            for (Vec next : neighbours(current)) {
                if (parents.containsKey(next)) continue;
                boolean isGoal = next.equals(goal);
                if (!isGoal && !passable.test(next)) continue;
                parents.put(next, current);
                if (isGoal) return reconstruct(parents, start, goal);
                if (parents.size() > maxNodes) return null;
                queue.add(next);
            }
        }
        return null;
    }

    /**
     * Returns the cell itself if it is passable, otherwise the passable cell which is nearest to it on the same y.
     * The cells are searched ring by ring around the cell, so the nearest ring wins.
     *
     * @param cell      the cell to start from
     * @param passable  checks if a cell can be walked on
     * @param maxRadius the largest ring around the cell which is searched
     * @return the cell itself if passable, the nearest passable cell in the radius, or the cell itself if there is none
     */
    static Vec nearestPassable(Vec cell, Predicate<Vec> passable, int maxRadius) {
        if (passable.test(cell)) return cell;
        for (int radius = 1; radius <= maxRadius; radius++) {
            Vec nearest = null;
            int nearestDistance = Integer.MAX_VALUE;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    Vec candidate = cell.add(dx, 0, dz);
                    int distance = dx * dx + dz * dz;
                    if (distance < nearestDistance && passable.test(candidate)) {
                        nearest = candidate;
                        nearestDistance = distance;
                    }
                }
            }
            if (nearest != null) return nearest;
        }
        return cell;
    }

    /**
     * Checks if a cell can be walked on: the block of the cell and the block above it are not solid. A cell in an
     * unloaded chunk is never passable, because the blocks of it can't be read.
     *
     * @param instance the instance with the blocks
     * @param cell     the cell to check
     * @return true if the cell is passable
     */
    static boolean isPassable(Instance instance, Vec cell) {
        if (!instance.isChunkLoaded(cell)) return false;
        return !instance.getBlock(cell).solid() && !instance.getBlock(cell.add(0, 1, 0)).solid();
    }

    /**
     * Returns the cell which contains the given coordinates, the coordinates are floored to whole blocks.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the cell
     */
    static Vec cell(double x, double y, double z) {
        return new Vec(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    /**
     * Returns the cell which contains the given point.
     *
     * @param point the point
     * @return the cell of the point
     */
    static Vec cellOf(Point point) {
        return cell(point.x(), point.y(), point.z());
    }

    private static List<Vec> neighbours(Vec cell) {
        return List.of(
                cell.add(1, 0, 0),
                cell.add(-1, 0, 0),
                cell.add(0, 0, 1),
                cell.add(0, 0, -1)
        );
    }

    private static List<Vec> reconstruct(Map<Vec, Vec> parents, Vec start, Vec goal) {
        List<Vec> route = new ArrayList<>();
        Vec cell = goal;
        while (!cell.equals(start)) {
            route.add(cell);
            cell = parents.get(cell);
        }
        Collections.reverse(route);
        return route;
    }
}
