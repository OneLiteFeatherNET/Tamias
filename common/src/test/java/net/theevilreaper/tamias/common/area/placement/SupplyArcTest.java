package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplyArcTest {

    private static final Vec FROM = new Vec(5, 50, 5);
    private static final Vec TO = new Vec(8, 50, 5);

    @Test
    void testPointsFollowAnArcBetweenTheBlocks() {
        List<Vec> points = SupplyArc.points(FROM, TO);

        assertEquals(SupplyArc.TICKS - 1, points.size());
        Vec middle = points.get(SupplyArc.TICKS / 2 - 1);
        assertEquals(6.5 + 0.5, middle.x(), 1e-9, "The middle point lies halfway between the block centers");
        assertEquals(50 + SupplyArc.HEIGHT, middle.y(), 1e-9, "The middle point is the apex of the arc");
        assertTrue(points.getFirst().x() < points.getLast().x());
    }

    @Test
    void testArcIsFreeWithoutBlocksInTheWay() {
        assertTrue(SupplyArc.isFree(FROM, TO, _ -> true));
    }

    @Test
    void testArcIsBlockedByAWallBetweenTheBlocks() {
        // A wall of three blocks between the impact and the target
        Set<Point> wall = Set.of(new Vec(6, 50, 5), new Vec(6, 51, 5), new Vec(6, 52, 5),
                new Vec(7, 50, 5), new Vec(7, 51, 5), new Vec(7, 52, 5));

        assertFalse(SupplyArc.isFree(FROM, TO, position -> !wall.contains(position)));
    }

    @Test
    void testArcIgnoresBlocksBelowTheFlight() {
        Set<Point> ground = Set.of(new Vec(6, 49, 5), new Vec(7, 49, 5));

        assertTrue(SupplyArc.isFree(FROM, TO, position -> !ground.contains(position)));
    }
}
