package net.theevilreaper.tamias.common.area;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
