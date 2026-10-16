package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.MAX_TNT_GROUP_SIZE;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.MIN_TNT_GROUP_SIZE;

/**
 * The {@link TntLayout} decides where the TNT blocks of a round are dropped.
 * A part of the TNT is dropped in small groups, the rest as single blocks. The groups and single blocks are spread
 * out over the area, so the TNT doesn't cover the whole area with single blocks.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TntLayout {

    /**
     * Plans the TNT groups for a round.
     *
     * @param candidates the positions where a TNT block can be placed
     * @param count      the amount of TNT blocks to place
     * @param groupRatio the share of TNT blocks which are placed in groups, between 0 and 1
     * @param random     the random generator to use
     * @return the planned groups, a single TNT block is a group without members
     */
    public static List<TntGroup> plan(Collection<Vec> candidates, int count, double groupRatio, RandomGenerator random) {
        if (candidates.isEmpty() || count <= 0) return List.of();

        int total = Math.min(count, candidates.size());
        List<Integer> groupSizes = splitIntoGroups((int) Math.round(total * groupRatio), random);
        int groupedBlocks = groupSizes.stream().mapToInt(Integer::intValue).sum();
        int singleBlocks = total - groupedBlocks;

        List<Vec> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);
        List<Vec> anchors = selectSpreadOut(shuffled, groupSizes.size() + singleBlocks);
        // The anchors are shuffled, so the groups don't end up in the same part of the area
        Collections.shuffle(anchors, random);

        Set<Vec> freePositions = new HashSet<>(candidates);
        anchors.forEach(freePositions::remove);

        List<TntGroup> groups = new ArrayList<>(anchors.size());
        for (int i = 0; i < anchors.size(); i++) {
            Vec anchor = anchors.get(i);
            int memberCount = i < groupSizes.size() ? groupSizes.get(i) - 1 : 0;
            groups.add(new TntGroup(anchor, takeNeighbours(anchor, memberCount, freePositions, random)));
        }
        return groups;
    }

    /**
     * Splits the given amount of blocks into group sizes. A single block which is left over is not part of a group.
     *
     * @param blocks the amount of blocks to split
     * @param random the random generator to use
     * @return the sizes of the groups
     */
    private static List<Integer> splitIntoGroups(int blocks, RandomGenerator random) {
        List<Integer> sizes = new ArrayList<>();
        int remaining = blocks;
        while (remaining >= MIN_TNT_GROUP_SIZE) {
            int size = random.nextInt(MIN_TNT_GROUP_SIZE, Math.min(MAX_TNT_GROUP_SIZE, remaining) + 1);
            sizes.add(size);
            remaining -= size;
        }
        return sizes;
    }

    /**
     * Takes free positions next to the given anchor. The group gets smaller if there are not enough free neighbours,
     * e.g. at the edge of a structure.
     *
     * @param anchor        the center of the group
     * @param amount        the amount of neighbours to take
     * @param freePositions the positions which are still free, the taken ones are removed
     * @param random        the random generator to use
     * @return the taken neighbours
     */
    private static List<Vec> takeNeighbours(Vec anchor, int amount, Set<Vec> freePositions, RandomGenerator random) {
        if (amount <= 0) return List.of();

        List<Vec> neighbours = new ArrayList<>(8);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Vec neighbour = anchor.add(x, 0, z);
                if (freePositions.contains(neighbour)) {
                    neighbours.add(neighbour);
                }
            }
        }
        Collections.shuffle(neighbours, random);

        List<Vec> taken = neighbours.subList(0, Math.min(amount, neighbours.size()));
        taken.forEach(freePositions::remove);
        return List.copyOf(taken);
    }

    /**
     * Picks up to {@code count} positions which are spread out over the given candidates.
     * The spacing starts at the value which would cover the area evenly and is reduced until enough positions fit.
     *
     * @param shuffledCandidates the candidates in a random order
     * @param count              the amount of positions to pick
     * @return the picked positions
     */
    private static List<Vec> selectSpreadOut(List<Vec> shuffledCandidates, int count) {
        List<Vec> selected = new ArrayList<>(count);
        if (count <= 0) return selected;
        double spacing = Math.sqrt((double) shuffledCandidates.size() / count);

        while (selected.size() < count && spacing >= 1.0) {
            double spacingSquared = spacing * spacing;
            for (Vec candidate : shuffledCandidates) {
                if (selected.size() >= count) break;
                if (selected.contains(candidate)) continue;

                boolean farEnough = true;
                for (Vec picked : selected) {
                    if (picked.distanceSquared(candidate) < spacingSquared) {
                        farEnough = false;
                        break;
                    }
                }
                if (farEnough) selected.add(candidate);
            }
            spacing -= 1.0;
        }

        // The candidate pool is too dense for the requested spacing - fill the rest regardless of distance.
        for (Vec candidate : shuffledCandidates) {
            if (selected.size() >= count) break;
            if (!selected.contains(candidate)) selected.add(candidate);
        }

        return selected;
    }

    private TntLayout() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
