package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
     * Finds the shortest route of cells from the start to the target with an A* search. The search is led by the
     * remaining distance to the target, so on an open area it only visits about as many cells as the route is long.
     * Of the shortest routes it prefers one with few turns, so the route runs in long straight parts.
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
        Map<Vec, Node> best = new HashMap<>();
        Node first = new Node(start, 0, distance(start, goal), 0);
        parents.put(start, start);
        best.put(start, first);
        // Equal estimates prefer the cell closer to the target, so an open area is crossed straight instead of in width.
        // After that fewer turns win, so of all shortest routes one with long straight parts is taken
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::estimate)
                .thenComparingInt(Node::remaining)
                .thenComparingInt(Node::turns));
        open.add(first);
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (best.get(current.cell()) != current) continue;
            Vec heading = current.cell().sub(parents.get(current.cell()));
            for (Vec next : neighbours(current.cell())) {
                int cost = current.cost() + 1;
                boolean turns = !heading.isZero() && !next.sub(current.cell()).equals(heading);
                int turnCount = current.turns() + (turns ? 1 : 0);
                Node known = best.get(next);
                if (known != null && (known.cost() < cost || (known.cost() == cost && known.turns() <= turnCount))) continue;
                boolean isGoal = next.equals(goal);
                if (!isGoal && !passable.test(next)) continue;
                parents.put(next, current.cell());
                Node node = new Node(next, cost, distance(next, goal), turnCount);
                best.put(next, node);
                // Every step costs the same and the estimate never overshoots, so the first route to the goal is the shortest
                if (isGoal) return reconstruct(parents, start, goal);
                if (parents.size() > maxNodes) return null;
                open.add(node);
            }
        }
        return null;
    }

    /**
     * Returns the number of steps between two cells on the same height when nothing is in the way.
     */
    private static int distance(Vec a, Vec b) {
        return (int) (Math.abs(a.x() - b.x()) + Math.abs(a.z() - b.z()));
    }

    /**
     * A cell of the open search with the steps it took to get there, the steps which are at least left and the turns
     * of the route so far.
     */
    private record Node(Vec cell, int cost, int remaining, int turns) {

        int estimate() {
            return this.cost + this.remaining;
        }
    }

    /**
     * Returns a check for cells which are free and stand on ground which is already placed.
     *
     * @param context the context of the run
     * @return the check for cells with placed ground
     */
    static Predicate<Vec> onGround(TntAnimationContext context) {
        return cell -> isPassable(context.instance(), cell) && context.hasGround(cell);
    }

    /**
     * Returns a check for cells which are free and lie over the game area, the ground below may still be missing.
     *
     * @param context the context of the run
     * @return the check for cells inside the area
     */
    static Predicate<Vec> inArea(TntAnimationContext context) {
        return cell -> isPassable(context.instance(), cell) && context.isInArea(cell);
    }

    /**
     * Finds a route which only crosses placed ground. Without one the route may cross ground which is still missing,
     * but it never leaves the game area.
     *
     * @param from    the cell to start at
     * @param to      the cell to reach
     * @param context the context of the run
     * @return the route as {@link #find(Vec, Vec, Predicate, int)} returns it, null if there is none in the area
     */
    static @Nullable List<Vec> findRoute(Vec from, Vec to, TntAnimationContext context) {
        List<Vec> route = find(from, to, onGround(context), MAX_ROUTE_NODES);
        if (route != null) return route;
        return find(from, to, inArea(context), MAX_ROUTE_NODES);
    }

    /**
     * Returns the nearest free cell with placed ground around the cell. Without one the nearest free cell inside the
     * game area is used, and without that the cell itself.
     *
     * @param cell    the cell to start from
     * @param context the context of the run
     * @param allowed an extra check a cell must pass, e.g. that no other entity uses it yet
     * @return the nearest cell to stand on
     */
    static Vec nearestStandable(Vec cell, TntAnimationContext context, Predicate<Vec> allowed) {
        Predicate<Vec> withGround = onGround(context).and(allowed);
        Vec nearest = nearestPassable(cell, withGround, MAX_SPAWN_RADIUS);
        if (withGround.test(nearest)) return nearest;
        return nearestPassable(cell, inArea(context).and(allowed), MAX_SPAWN_RADIUS);
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
