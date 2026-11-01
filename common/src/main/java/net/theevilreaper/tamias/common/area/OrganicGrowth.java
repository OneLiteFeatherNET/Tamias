package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

/**
 * Calculates the fill order of the {@link GroundFillPattern#ORGANIC} pattern.
 * The growth starts at the ground position closest to the center and spreads from block to neighbouring block,
 * each block has a cost to grow onto it (Dijkstra with random weights). The costs follow a smooth random field
 * (value noise), so the slow blocks form connected patches and the growth grows tendrils around them.
 * Once a part of the ground is filled, the next part starts at its position closest to the center.
 * <p>
 * All data is kept in arrays over the bounding box of the ground, so the calculation stays cheap on large areas.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
final class OrganicGrowth {

    /**
     * The size in blocks of the patches which slow down the growth.
     */
    private static final int PATCH_SIZE = 5;
    /**
     * The highest extra cost of a block within a slow patch, a block outside of the patches costs 1.
     */
    private static final double SLOW_COST = 15;

    private final int minX;
    private final int minZ;
    private final int width;
    private final int depth;
    private final boolean[] ground;
    private final double[] cost;
    private final double[] arrival;

    private OrganicGrowth(Collection<Vec> positions, RandomGenerator random) {
        int lowX = Integer.MAX_VALUE;
        int lowZ = Integer.MAX_VALUE;
        int highX = Integer.MIN_VALUE;
        int highZ = Integer.MIN_VALUE;
        for (Vec position : positions) {
            lowX = Math.min(lowX, position.blockX());
            lowZ = Math.min(lowZ, position.blockZ());
            highX = Math.max(highX, position.blockX());
            highZ = Math.max(highZ, position.blockZ());
        }
        this.minX = lowX;
        this.minZ = lowZ;
        this.width = highX - lowX + 1;
        this.depth = highZ - lowZ + 1;
        this.ground = new boolean[this.width * this.depth];
        for (Vec position : positions) {
            this.ground[index(position.blockX() - this.minX, position.blockZ() - this.minZ)] = true;
        }
        this.cost = createCosts(random);
        this.arrival = new double[this.ground.length];
        Arrays.fill(this.arrival, Double.MAX_VALUE);
    }

    /**
     * Calculates the organic growth over the given positions.
     *
     * @param positions the ground positions, must not be empty
     * @param center    the center of the area
     * @param random    the random generator for the costs
     * @return the fill order, the key is the time the growth reaches the position
     */
    static ToDoubleFunction<Point> grow(Collection<Vec> positions, Point center, RandomGenerator random) {
        OrganicGrowth growth = new OrganicGrowth(positions, random);
        growth.spread(center);
        return growth::arrivalAt;
    }

    /**
     * Returns the time the growth reaches the column of the given position, the height is ignored.
     *
     * @param position the position
     * @return the arrival time, {@link Double#MAX_VALUE} if the position is not part of the ground
     */
    private double arrivalAt(Point position) {
        int x = position.blockX() - this.minX;
        int z = position.blockZ() - this.minZ;
        if (x < 0 || z < 0 || x >= this.width || z >= this.depth) return Double.MAX_VALUE;
        return this.arrival[index(x, z)];
    }

    /**
     * Spreads the growth over every part of the ground. The parts are started by their distance to the center.
     *
     * @param center the center of the area
     */
    private void spread(Point center) {
        Integer[] byDistance = new Integer[this.ground.length];
        int count = 0;
        for (int i = 0; i < this.ground.length; i++) {
            if (this.ground[i]) byDistance[count++] = i;
        }
        double centerX = center.x() - this.minX;
        double centerZ = center.z() - this.minZ;
        Arrays.sort(byDistance, 0, count, Comparator.comparingDouble(i -> {
            double dx = (i / this.depth) - centerX;
            double dz = (i % this.depth) - centerZ;
            return dx * dx + dz * dz;
        }));

        double offset = 0;
        for (int i = 0; i < count; i++) {
            int start = byDistance[i];
            if (this.arrival[start] != Double.MAX_VALUE) continue;
            offset = spreadFrom(start, offset) + 1;
        }
    }

    /**
     * Spreads the growth from the given start over its part of the ground.
     *
     * @param start  the index of the first block
     * @param offset the time the growth starts at
     * @return the time the last block of the part is reached
     */
    private double spreadFrom(int start, double offset) {
        double latest = offset;
        PriorityQueue<Step> queue = new PriorityQueue<>(Comparator.comparingDouble(Step::time));
        queue.add(new Step(start, offset));
        this.arrival[start] = offset;
        while (!queue.isEmpty()) {
            Step step = queue.poll();
            if (step.time() > this.arrival[step.index()]) continue;
            latest = Math.max(latest, step.time());
            int x = step.index() / this.depth;
            int z = step.index() % this.depth;
            if (x > 0) relax(index(x - 1, z), step.time(), queue);
            if (x < this.width - 1) relax(index(x + 1, z), step.time(), queue);
            if (z > 0) relax(index(x, z - 1), step.time(), queue);
            if (z < this.depth - 1) relax(index(x, z + 1), step.time(), queue);
        }
        return latest;
    }

    private void relax(int next, double time, PriorityQueue<Step> queue) {
        if (!this.ground[next]) return;
        double arrive = time + this.cost[next];
        if (arrive < this.arrival[next]) {
            this.arrival[next] = arrive;
            queue.add(new Step(next, arrive));
        }
    }

    /**
     * Creates the cost of every block from a smooth random field.
     * The random values sit on the corners of patches and are blended in between.
     *
     * @param random the random generator for the corners
     * @return the costs, at least 1
     */
    private double[] createCosts(RandomGenerator random) {
        int cellsX = this.width / PATCH_SIZE + 2;
        int cellsZ = this.depth / PATCH_SIZE + 2;
        double[] corners = new double[cellsX * cellsZ];
        for (int i = 0; i < corners.length; i++) {
            corners[i] = random.nextDouble();
        }
        double[] costs = new double[this.ground.length];
        for (int x = 0; x < this.width; x++) {
            int cellX = x / PATCH_SIZE;
            double fx = smooth((double) (x % PATCH_SIZE) / PATCH_SIZE);
            for (int z = 0; z < this.depth; z++) {
                int cellZ = z / PATCH_SIZE;
                double fz = smooth((double) (z % PATCH_SIZE) / PATCH_SIZE);
                double top = lerp(corners[cellX * cellsZ + cellZ], corners[(cellX + 1) * cellsZ + cellZ], fx);
                double bottom = lerp(corners[cellX * cellsZ + cellZ + 1], corners[(cellX + 1) * cellsZ + cellZ + 1], fx);
                double noise = lerp(top, bottom, fz);
                // Only the upper part of the field slows the growth down, so most of the ground grows fast
                double slow = Math.max(0, noise - 0.5) * 2;
                costs[index(x, z)] = 1 + SLOW_COST * slow * slow;
            }
        }
        return costs;
    }

    private int index(int x, int z) {
        return x * this.depth + z;
    }

    private static double smooth(double value) {
        return value * value * (3 - 2 * value);
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private record Step(int index, double time) { }
}
