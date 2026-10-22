package net.theevilreaper.tamias.common.util;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelperTest {

    @Test
    void testPositionComparator() {
        Comparator<Vec> comparator = Helper.getComparator(Vec.ZERO);
        List<Vec> positions = new ArrayList<>(List.of(
                new Vec(5, 0, 5),
                new Vec(1, 0, 1),
                new Vec(0, 0, 0),
                new Vec(3, 0, 0)
        ));

        positions.sort(comparator);

        assertEquals(
                List.of(
                        new Vec(0, 0, 0),
                        new Vec(1, 0, 1),
                        new Vec(3, 0, 0),
                        new Vec(5, 0, 5)
                ),
                positions
        );
    }

    @Test
    void testPositionComparatorUsesCenter() {
        Comparator<Vec> comparator = Helper.getComparator(new Vec(10, 0, -10));
        List<Vec> positions = new ArrayList<>(List.of(
                new Vec(0, 0, 0),
                new Vec(12, 0, -10),
                new Vec(10, 0, -10)
        ));

        positions.sort(comparator);

        assertEquals(
                List.of(
                        new Vec(10, 0, -10),
                        new Vec(12, 0, -10),
                        new Vec(0, 0, 0)
                ),
                positions
        );
    }
}
