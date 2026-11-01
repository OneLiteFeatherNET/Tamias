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
