package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.theevilreaper.tamias.common.area.GameAreaHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TntLayoutTest {

    private static List<Vec> grid(int size) {
        List<Vec> positions = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                positions.add(new Vec(x, 1, z));
            }
        }
        return positions;
    }

    private static double horizontalDistanceSquared(Vec a, Vec b) {
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return dx * dx + dz * dz;
    }

    private static int blockCount(List<TntGroup> groups) {
        return groups.stream().mapToInt(group -> group.positions().size()).sum();
    }

    @Test
    void testNoCandidatesPlanNothing() {
        assertTrue(TntLayout.plan(List.of(), 10, 0.4, new Random(1)).isEmpty());
        assertTrue(TntLayout.plan(grid(5), 0, 0.4, new Random(1)).isEmpty());
    }

    @Test
    void testWithoutGroupRatioOnlySingleBlocksArePlanned() {
        List<TntGroup> groups = TntLayout.plan(grid(20), 15, 0, new Random(2));

        assertEquals(15, groups.size());
        assertTrue(groups.stream().allMatch(group -> group.members().isEmpty()));
    }

    @Test
    void testGroupRatioSplitsTheBlocks() {
        List<TntGroup> groups = TntLayout.plan(grid(30), 25, 0.4, new Random(3));

        assertEquals(25, blockCount(groups));
        int groupedBlocks = groups.stream()
                .filter(group -> !group.members().isEmpty())
                .mapToInt(group -> group.positions().size())
                .sum();
        // 40 percent of 25 blocks are 10 blocks, a single block which is left over is not part of a group
        assertTrue(groupedBlocks == 10 || groupedBlocks == 9, "Expected 9 or 10 grouped blocks, got " + groupedBlocks);
    }

    @Test
    void testGroupsAreSmallAndAroundTheirCenter() {
        List<Vec> candidates = grid(30);
        List<TntGroup> groups = TntLayout.plan(candidates, 30, 1, new Random(4));

        Set<Vec> usedPositions = new HashSet<>();
        for (TntGroup group : groups) {
            assertTrue(group.positions().size() <= GameAreaHelper.MAX_TNT_GROUP_SIZE);
            for (Vec member : group.members()) {
                Vec offset = member.sub(group.center());
                assertTrue(Math.abs(offset.x()) <= 1 && Math.abs(offset.z()) <= 1 && offset.y() == 0,
                        member + " is not next to " + group.center());
            }
            for (Vec position : group.positions()) {
                assertTrue(candidates.contains(position), position + " is no candidate");
                assertTrue(usedPositions.add(position), position + " is used twice");
            }
        }
        assertEquals(30, blockCount(groups));
    }

    @Test
    void testMissingBlocksAreBackfilledWithoutFreeNeighbours() {
        // The candidates are too far apart to form a group
        List<Vec> candidates = List.of(new Vec(0, 1, 0), new Vec(5, 1, 0), new Vec(10, 1, 0), new Vec(15, 1, 0));
        List<TntGroup> groups = TntLayout.plan(candidates, 4, 1, new Random(5));

        assertTrue(groups.stream().allMatch(group -> group.members().isEmpty()));
        assertEquals(4, blockCount(groups), "All requested TNT blocks should be dropped as single blocks");
    }

    @Test
    void testNoPositionIsUsedTwiceWhenNeighboursAreScarce() {
        // Only the direct left and right neighbours of a candidate are free, so the groups have to be small
        List<Vec> candidates = new ArrayList<>();
        for (int x = 0; x < 10; x++) {
            candidates.add(new Vec(x, 1, 0));
        }
        List<TntGroup> groups = TntLayout.plan(candidates, 10, 1, new Random(8));

        Set<Vec> usedPositions = new HashSet<>();
        for (TntGroup group : groups) {
            for (Vec position : group.positions()) {
                assertTrue(usedPositions.add(position), position + " is used twice");
            }
        }
        assertEquals(10, blockCount(groups), "All requested TNT blocks should be dropped");
    }

    @Test
    void testCountIsLimitedByTheCandidates() {
        List<TntGroup> groups = TntLayout.plan(grid(2), 10, 0, new Random(6));

        assertEquals(4, blockCount(groups));
    }

    @Test
    void testSingleBlocksAreSpreadOut() {
        List<TntGroup> groups = TntLayout.plan(grid(10), 9, 0, new Random(7));

        double minDistance = Double.MAX_VALUE;
        for (TntGroup a : groups) {
            for (TntGroup b : groups) {
                if (a == b) continue;
                minDistance = Math.min(minDistance, a.center().distance(b.center()));
            }
        }
        assertTrue(minDistance >= 2.0, "TNT positions should be spread apart, closest pair was " + minDistance + " blocks apart");
    }

    @Test
    void testSupplyDropIsPlannedWithMembersAroundItsCenter() {
        List<TntGroup> groups = TntLayout.plan(grid(30), 25, 0.4, 1, _ -> true, new Random(7));
        List<TntGroup> supply = groups.stream().filter(TntGroup::isSupply).toList();

        assertEquals(1, supply.size());
        TntGroup drop = supply.getFirst();
        int members = drop.members().size();
        assertTrue(members >= GameAreaHelper.MIN_SUPPLY_DROP_MEMBERS && members <= GameAreaHelper.MAX_SUPPLY_DROP_MEMBERS,
                "Expected 3 to 5 members, got " + members);
        double radius = GameAreaHelper.SUPPLY_SCATTER_RADIUS;
        assertTrue(drop.members().stream().allMatch(member -> horizontalDistanceSquared(member, drop.center()) <= radius * radius));
        assertEquals(25, blockCount(groups));
    }

    @Test
    void testSupplyDropsKeepTheirDistance() {
        List<TntGroup> supply = TntLayout.plan(grid(40), 30, 0.4, 2, _ -> true, new Random(8))
                .stream().filter(TntGroup::isSupply).toList();

        assertEquals(2, supply.size());
        double min = 2 * GameAreaHelper.SUPPLY_SCATTER_RADIUS + 1;
        assertTrue(horizontalDistanceSquared(supply.get(0).center(), supply.get(1).center()) >= min * min);
    }

    @Test
    void testNoSupplyDropWithoutFreeHighPath() {
        List<TntGroup> groups = TntLayout.plan(grid(30), 25, 0.4, 2, _ -> false, new Random(9));

        assertTrue(groups.stream().noneMatch(TntGroup::isSupply));
        assertEquals(25, blockCount(groups));
    }

    @Test
    void testPositionsAreNotPlannedTwice() {
        List<TntGroup> groups = TntLayout.plan(grid(30), 25, 0.4, 2, _ -> true, new Random(10));
        Set<Vec> unique = new HashSet<>();

        groups.forEach(group -> group.positions().forEach(position -> assertTrue(unique.add(position), "Planned twice: " + position)));
    }

    @Test
    void testSupplyDropPrefersAnAnchorWithRoomForItsMembers() {
        // Many isolated positions without neighbours and one small field where a supply drop has enough room
        List<Vec> candidates = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            candidates.add(new Vec(100 + i * 10, 1, 100));
        }
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                candidates.add(new Vec(x, 1, z));
            }
        }

        TntGroup drop = TntLayout.plan(candidates, 20, 0.4, 1, _ -> true, new Random(11))
                .stream().filter(TntGroup::isSupply).findFirst().orElseThrow();

        assertTrue(drop.members().size() >= GameAreaHelper.MIN_SUPPLY_DROP_MEMBERS,
                "The supply drop should land where its members have room, got " + drop.members().size());
    }

    @Test
    void testSupplyDropOnlyTakesMembersWithAFreeArc() {
        // Only the members on the left of the anchor can be reached
        List<TntGroup> groups = TntLayout.plan(grid(30), 25, 0.4, 1, _ -> true,
                (anchor, member) -> member.x() < anchor.x(), new Random(12));
        TntGroup drop = groups.stream().filter(TntGroup::isSupply).findFirst().orElseThrow();

        assertFalse(drop.members().isEmpty());
        assertTrue(drop.members().stream().allMatch(member -> member.x() < drop.center().x()));
        assertEquals(25, blockCount(groups));
    }
}
