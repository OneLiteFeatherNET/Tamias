package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundFillPatternTest {

    @Test
    void testLinePatternAlternatesDirectionPerRow() {
        List<Vec> expected = List.of(
                new Vec(0, 0, 0), new Vec(1, 0, 0), new Vec(2, 0, 0),
                new Vec(2, 0, 1), new Vec(1, 0, 1), new Vec(0, 0, 1),
                new Vec(0, 0, 2), new Vec(1, 0, 2), new Vec(2, 0, 2)
        );

        List<Vec> shuffled = new ArrayList<>(expected);
        Collections.shuffle(shuffled);
        shuffled.sort(GroundFillPattern.LINE.comparator(GroundFillPattern.Context.of(Vec.ZERO)));

        assertEquals(expected, shuffled);
    }

    @Test
    void testNextPatternNeverRepeatsThePrevious() {
        Random random = new Random(7);
        GroundFillPattern previous = null;
        Set<GroundFillPattern> seen = EnumSet.noneOf(GroundFillPattern.class);
        for (int i = 0; i < 200; i++) {
            GroundFillPattern next = GroundFillPattern.next(previous, random);
            assertNotEquals(previous, next);
            seen.add(next);
            previous = next;
        }
        assertEquals(EnumSet.allOf(GroundFillPattern.class), seen, "Every pattern should be picked at some point");
    }

    @Test
    void testGalaxyArmsGrowAheadOfTheGaps() {
        Vec center = new Vec(50, 0, 50);
        // Without rotation an arm passes the angle radius * twist, the gap lies half a sector further
        Vec arm = onCircle(center, 10, 10 * 0.25);
        Vec otherArm = onCircle(center, 10, 10 * 0.25 + Math.TAU / 3);
        Vec gap = onCircle(center, 5, 5 * 0.25 + Math.TAU / 6);

        List<Vec> positions = new ArrayList<>(List.of(gap, arm, center, otherArm));
        positions.sort(GroundFillPattern.galaxyComparator(center, 0));

        assertEquals(center, positions.getFirst());
        assertEquals(Set.of(arm, otherArm), Set.copyOf(positions.subList(1, 3)), "Both arms reach a radius of 10 at the same time");
        assertEquals(gap, positions.getLast(), "The gap between the arms fills behind the arms");
    }

    @Test
    void testDropSpotsAreSpreadOverTheArea() {
        List<Vec> grid = grid(30);
        List<Vec> spots = GroundFillPattern.pickDropSpots(grid, 4, new Random(3));

        assertEquals(4, Set.copyOf(spots).size());
        assertTrue(grid.containsAll(spots));
        for (Vec spot : spots) {
            for (Vec other : spots) {
                if (spot != other) {
                    assertTrue(spot.distance(other) > 5, spot + " and " + other + " are too close");
                }
            }
        }
    }

    @Test
    void testDropSpotsAreNotPickedAtTheEdge() {
        Vec center = new Vec(15, 0, 15);
        List<Vec> inner = GroundFillPattern.innerPositions(grid(31), center);

        assertTrue(inner.contains(center));
        assertTrue(inner.contains(new Vec(15, 0, 6)), "9 blocks from the center is within 60 % of the largest distance");
        assertFalse(inner.contains(new Vec(15, 0, 0)), "The edge is too far away from the center");
        assertFalse(inner.contains(new Vec(0, 0, 0)), "The corner is too far away from the center");
    }

    @Test
    void testDropsGrowFromEverySpot() {
        List<Vec> spots = List.of(new Vec(0, 0, 0), new Vec(20, 0, 0));
        List<Vec> positions = new ArrayList<>(List.of(
                new Vec(10, 0, 0), new Vec(21, 0, 0), new Vec(0, 0, 0), new Vec(3, 0, 0), new Vec(20, 0, 0)
        ));

        positions.sort(GroundFillPattern.dropsComparator(spots, Vec.ZERO));

        assertEquals(Set.copyOf(spots), Set.copyOf(positions.subList(0, 2)), "Every spot starts at the same time");
        assertEquals(List.of(new Vec(21, 0, 0), new Vec(3, 0, 0), new Vec(10, 0, 0)), positions.subList(2, 5));
    }

    @Test
    void testDropsFallBackToTheCenterWithoutPositions() {
        assertTrue(GroundFillPattern.pickDropSpots(List.of(), 3, new Random(1)).isEmpty());
        List<Vec> positions = new ArrayList<>(List.of(new Vec(5, 0, 0), new Vec(1, 0, 0)));
        positions.sort(GroundFillPattern.dropsComparator(List.of(), Vec.ZERO));
        assertEquals(new Vec(1, 0, 0), positions.getFirst());
    }

    @Test
    void testOrganicGrowthSpreadsFromBlockToBlock() {
        List<Vec> positions = grid(20);
        Vec center = new Vec(10, 0, 10);
        positions.sort(GroundFillPattern.organicComparator(new GroundFillPattern.Context(center, positions, new Random(5))));

        assertEquals(center, positions.getFirst());
        assertGrowsFromNeighbours(positions, 1);
    }

    @Test
    void testOrganicGrowthFillsSeparatePartsOneAfterAnother() {
        List<Vec> near = new ArrayList<>();
        List<Vec> far = new ArrayList<>();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                near.add(new Vec(x, 0, z));
                far.add(new Vec(x + 10, 0, z));
            }
        }
        List<Vec> positions = new ArrayList<>(far);
        positions.addAll(near);
        positions.sort(GroundFillPattern.organicComparator(new GroundFillPattern.Context(Vec.ZERO, positions, new Random(5))));

        assertEquals(Set.copyOf(near), Set.copyOf(positions.subList(0, 25)), "The part closest to the center grows first");
        assertEquals(Set.copyOf(far), Set.copyOf(positions.subList(25, 50)));
        assertGrowsFromNeighbours(positions, 2);
    }

    @Test
    void testOrganicGrowthIgnoresTheHeight() {
        List<Vec> ground = grid(10);
        Comparator<Vec> comparator = GroundFillPattern.organicComparator(new GroundFillPattern.Context(Vec.ZERO, ground, new Random(9)));
        List<Vec> sortedGround = new ArrayList<>(ground);
        sortedGround.sort(comparator);
        List<Vec> above = new ArrayList<>(ground.stream().map(pos -> pos.add(0, 1, 0)).toList());
        above.sort(comparator);

        assertEquals(sortedGround, above.stream().map(pos -> pos.sub(0, 1, 0)).toList(), "TNT above the ground follows the same order");
    }

    /**
     * Checks that every position except the start of each part has a neighbour which was placed before.
     */
    private static void assertGrowsFromNeighbours(List<Vec> order, int parts) {
        Set<Vec> placed = new HashSet<>();
        int starts = 0;
        for (Vec pos : order) {
            boolean hasPlacedNeighbour = placed.contains(pos.add(1, 0, 0)) || placed.contains(pos.sub(1, 0, 0))
                    || placed.contains(pos.add(0, 0, 1)) || placed.contains(pos.sub(0, 0, 1));
            if (!hasPlacedNeighbour) starts++;
            placed.add(pos);
        }
        assertEquals(parts, starts, "Every part should grow from a single start");
    }

    private static List<Vec> grid(int size) {
        List<Vec> grid = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                grid.add(new Vec(x, 0, z));
            }
        }
        return grid;
    }

    private static Vec onCircle(Vec center, double radius, double angle) {
        return center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
    }

    @Test
    void testCirclePatternGrowsFromTheGivenCenter() {
        Vec center = new Vec(100, 0, 100);
        List<Vec> positions = new ArrayList<>(List.of(
                new Vec(103, 0, 100),
                new Vec(100, 0, 100),
                new Vec(98, 0, 100),
                new Vec(101, 0, 101)
        ));

        positions.sort(GroundFillPattern.CIRCLE.comparator(GroundFillPattern.Context.of(center)));

        assertEquals(
                List.of(
                        new Vec(100, 0, 100),
                        new Vec(101, 0, 101),
                        new Vec(98, 0, 100),
                        new Vec(103, 0, 100)
                ),
                positions
        );
    }
}
