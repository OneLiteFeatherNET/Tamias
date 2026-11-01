package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
