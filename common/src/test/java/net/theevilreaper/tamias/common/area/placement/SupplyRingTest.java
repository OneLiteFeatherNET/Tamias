package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplyRingTest {

    private static List<Vec> grid(int fromX, int size) {
        List<Vec> positions = new ArrayList<>();
        for (int x = fromX; x < size; x++) {
            for (int z = 0; z < size; z++) {
                positions.add(new Vec(x, 49, z));
            }
        }
        return positions;
    }

    @Test
    void testRingIsAroundTheCenter() {
        List<Vec> ring = SupplyRing.positions(new Vec(10, 49, 10), grid(0, 21));

        assertTrue(ring.contains(new Vec(12, 49, 10)));
        assertTrue(ring.contains(new Vec(10, 49, 8)));
        assertFalse(ring.contains(new Vec(10, 49, 10)), "The center keeps the normal ground");
        assertFalse(ring.contains(new Vec(11, 49, 10)));
        assertFalse(ring.contains(new Vec(14, 49, 10)));
    }

    @Test
    void testRingStaysInsideTheArea() {
        List<Vec> ring = SupplyRing.positions(new Vec(10, 49, 10), grid(10, 21));

        assertFalse(ring.isEmpty());
        assertTrue(ring.stream().allMatch(position -> position.x() >= 10));
    }

    @Test
    void testRingOnlyUsesTheGroundHeight() {
        List<Vec> ring = SupplyRing.positions(new Vec(10, 49, 10), List.of(new Vec(12, 50, 10), new Vec(12, 49, 10)));

        assertTrue(ring.stream().allMatch(position -> position.y() == 49));
    }
}
