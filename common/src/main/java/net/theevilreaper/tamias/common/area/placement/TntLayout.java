package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import static net.theevilreaper.tamias.common.area.GameAreaHelper.MAX_SUPPLY_DROP_MEMBERS;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.MAX_TNT_GROUP_SIZE;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.MIN_SUPPLY_DROP_MEMBERS;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.MIN_TNT_GROUP_SIZE;
import static net.theevilreaper.tamias.common.area.GameAreaHelper.SUPPLY_SCATTER_RADIUS;

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
        return plan(candidates, count, groupRatio, 0, _ -> true, random);
    }

    /**
     * Plans the TNT groups for a round with supply drops. The supply drops are planned first, so they get a free spot
     * with enough space around it. The remaining TNT is planned like a round without supply drops.
     *
     * @param candidates        the positions where a TNT block can be placed
     * @param count             the amount of TNT blocks to place, the TNT of the supply drops is part of it
     * @param groupRatio        the share of the remaining TNT blocks which are placed in groups, between 0 and 1
     * @param supplyDrops       the amount of supply drops to plan, fewer are planned if no anchor fits
     * @param supplyAnchorCheck checks if a supply drop can fall down onto a position
     * @param random            the random generator to use
     * @return the planned groups, the supply drops first
     */
    public static List<TntGroup> plan(
            Collection<Vec> candidates,
            int count,
            double groupRatio,
            int supplyDrops,
            Predicate<Vec> supplyAnchorCheck,
            RandomGenerator random
    ) {
        return plan(candidates, count, groupRatio, supplyDrops, supplyAnchorCheck, (_, _) -> true, random);
    }

    /**
     * Plans the TNT groups for a round with supply drops. The supply drops are planned first, so they get a free spot
     * with enough space around it. The remaining TNT is planned like a round without supply drops.
     *
     * @param candidates        the positions where a TNT block can be placed
     * @param count             the amount of TNT blocks to place, the TNT of the supply drops is part of it
     * @param groupRatio        the share of the remaining TNT blocks which are placed in groups, between 0 and 1
     * @param supplyDrops       the amount of supply drops to plan, fewer are planned if no anchor fits
     * @param supplyAnchorCheck checks if a supply drop can fall down onto a position
     * @param memberCheck       checks if a member can fly from the anchor of a supply drop to a position
     * @param random            the random generator to use
     * @return the planned groups, the supply drops first
     */
    public static List<TntGroup> plan(
            Collection<Vec> candidates,
            int count,
            double groupRatio,
            int supplyDrops,
            Predicate<Vec> supplyAnchorCheck,
            BiPredicate<Vec, Vec> memberCheck,
            RandomGenerator random
    ) {
        if (candidates.isEmpty() || count <= 0) return List.of();
        if (supplyDrops <= 0) return planGroups(candidates, count, groupRatio, random);

        int total = Math.min(count, candidates.size());
        Set<Vec> freePositions = new HashSet<>(candidates);
        List<TntGroup> groups = new ArrayList<>(planSupplyDrops(candidates, total, supplyDrops, supplyAnchorCheck, memberCheck, freePositions, random));
        if (groups.isEmpty()) return planGroups(candidates, count, groupRatio, random);

        int supplyBlocks = groups.stream().mapToInt(group -> group.positions().size()).sum();
        List<Vec> remaining = new ArrayList<>(freePositions.size());
        for (Vec candidate : candidates) {
            if (freePositions.contains(candidate)) remaining.add(candidate);
        }
        if (total > supplyBlocks && !remaining.isEmpty()) {
            groups.addAll(planGroups(remaining, total - supplyBlocks, groupRatio, random));
        }
        return groups;
    }

    /**
     * Plans the supply drops. An anchor needs a free fall path and keeps enough distance to the other supply drops,
     * so their rings and the areas where their members land don't overlap. Anchors with room for at least
     * {@link net.theevilreaper.tamias.common.area.GameAreaHelper#MIN_SUPPLY_DROP_MEMBERS} members are preferred, the
     * other anchors are only used if no such anchor is left.
     *
     * @param candidates    the positions where a TNT block can be placed
     * @param total         the amount of TNT blocks of the round
     * @param supplyDrops   the amount of supply drops to plan
     * @param anchorCheck   checks if a supply drop can fall down onto a position
     * @param memberCheck   checks if a member can fly from the anchor to a position
     * @param freePositions the positions which are still free, the taken ones are removed
     * @param random        the random generator to use
     * @return the planned supply drops
     */
    private static List<TntGroup> planSupplyDrops(
            Collection<Vec> candidates,
            int total,
            int supplyDrops,
            Predicate<Vec> anchorCheck,
            BiPredicate<Vec, Vec> memberCheck,
            Set<Vec> freePositions,
            RandomGenerator random
    ) {
        List<Vec> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);
        double minDistance = 2.0 * SUPPLY_SCATTER_RADIUS + 1;
        int blocks = 0;

        List<TntGroup> drops = new ArrayList<>(supplyDrops);
        for (boolean requireRoom : new boolean[]{true, false}) {
            for (Vec anchor : shuffled) {
                if (drops.size() >= supplyDrops || blocks >= total) return drops;
                if (!freePositions.contains(anchor) || !anchorCheck.test(anchor)) continue;
                if (!isFarEnough(anchor, drops, minDistance)) continue;
                if (requireRoom && countWithinRadius(anchor, freePositions, memberCheck) < MIN_SUPPLY_DROP_MEMBERS) continue;

                freePositions.remove(anchor);
                int memberCount = Math.min(random.nextInt(MIN_SUPPLY_DROP_MEMBERS, MAX_SUPPLY_DROP_MEMBERS + 1), total - blocks - 1);
                List<Vec> members = takeWithinRadius(anchor, memberCount, freePositions, memberCheck, random);
                drops.add(new TntGroup(anchor, members, TntDropType.SUPPLY, List.of()));
                blocks += 1 + members.size();
            }
        }
        return drops;
    }

    /**
     * Counts the free positions around the given anchor where a member of a supply drop can land.
     *
     * @param anchor        the center of the supply drop
     * @param freePositions the positions which are still free
     * @param memberCheck   checks if a member can fly from the anchor to a position
     * @return the amount of reachable free positions within the scatter radius, without the anchor
     */
    private static int countWithinRadius(Vec anchor, Set<Vec> freePositions, BiPredicate<Vec, Vec> memberCheck) {
        double radiusSquared = (double) SUPPLY_SCATTER_RADIUS * SUPPLY_SCATTER_RADIUS;
        int count = 0;
        for (Vec position : freePositions) {
            if (!position.equals(anchor) && horizontalDistanceSquared(position, anchor) <= radiusSquared
                    && memberCheck.test(anchor, position)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isFarEnough(Vec position, List<TntGroup> drops, double minDistance) {
        for (TntGroup drop : drops) {
            if (horizontalDistanceSquared(position, drop.center()) < minDistance * minDistance) return false;
        }
        return true;
    }

    /**
     * Takes free positions within {@link net.theevilreaper.tamias.common.area.GameAreaHelper#SUPPLY_SCATTER_RADIUS}
     * around the given anchor.
     *
     * @param anchor        the center of the supply drop
     * @param amount        the amount of positions to take
     * @param freePositions the positions which are still free, the taken ones are removed
     * @param memberCheck   checks if a member can fly from the anchor to a position
     * @param random        the random generator to use
     * @return the taken positions
     */
    private static List<Vec> takeWithinRadius(
            Vec anchor,
            int amount,
            Set<Vec> freePositions,
            BiPredicate<Vec, Vec> memberCheck,
            RandomGenerator random
    ) {
        if (amount <= 0) return List.of();

        double radiusSquared = (double) SUPPLY_SCATTER_RADIUS * SUPPLY_SCATTER_RADIUS;
        List<Vec> nearby = new ArrayList<>();
        for (Vec position : freePositions) {
            if (horizontalDistanceSquared(position, anchor) <= radiusSquared && memberCheck.test(anchor, position)) {
                nearby.add(position);
            }
        }
        // The set has no stable order, so the positions are sorted before they are shuffled with the given random
        nearby.sort(Comparator.comparingDouble(Vec::x).thenComparingDouble(Vec::y).thenComparingDouble(Vec::z));
        Collections.shuffle(nearby, random);

        List<Vec> taken = nearby.subList(0, Math.min(amount, nearby.size()));
        taken.forEach(freePositions::remove);
        return List.copyOf(taken);
    }

    private static double horizontalDistanceSquared(Vec a, Vec b) {
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return dx * dx + dz * dz;
    }

    /**
     * Plans the normal groups and single blocks.
     *
     * @param candidates the positions where a TNT block can be placed
     * @param count      the amount of TNT blocks to place
     * @param groupRatio the share of TNT blocks which are placed in groups, between 0 and 1
     * @param random     the random generator to use
     * @return the planned groups
     */
    private static List<TntGroup> planGroups(Collection<Vec> candidates, int count, double groupRatio, RandomGenerator random) {

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

        // Groups which lost neighbours are topped up with single blocks, so the requested amount is dropped
        int missing = total - groups.stream().mapToInt(group -> group.positions().size()).sum();
        if (missing > 0) {
            List<Vec> remaining = new ArrayList<>(freePositions.size());
            for (Vec candidate : candidates) {
                if (freePositions.contains(candidate)) remaining.add(candidate);
            }
            Collections.shuffle(remaining, random);
            for (Vec position : selectSpreadOut(remaining, missing)) {
                groups.add(new TntGroup(position, List.of()));
            }
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
