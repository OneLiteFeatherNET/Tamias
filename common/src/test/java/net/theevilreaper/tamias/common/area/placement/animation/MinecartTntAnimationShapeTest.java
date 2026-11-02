package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinecartTntAnimationShapeTest {

    private static final Vec CURRENT = new Vec(5, 50, 5);
    private static final Vec NORTH = new Vec(5, 50, 4);
    private static final Vec SOUTH = new Vec(5, 50, 6);
    private static final Vec EAST = new Vec(6, 50, 5);
    private static final Vec WEST = new Vec(4, 50, 5);

    @Test
    void testStraightAlongXIsEastWest() {
        assertEquals("east_west", MinecartTntAnimation.railShape(WEST, CURRENT, EAST));
    }

    @Test
    void testStraightAlongZIsNorthSouth() {
        assertEquals("north_south", MinecartTntAnimation.railShape(NORTH, CURRENT, SOUTH));
    }

    @Test
    void testCellAtTheEndOfTheRouteUsesTheAxisOfItsNeighbour() {
        assertEquals("east_west", MinecartTntAnimation.railShape(WEST, CURRENT, null));
        assertEquals("north_south", MinecartTntAnimation.railShape(SOUTH, CURRENT, null));
    }

    @Test
    void testTurnBetweenNorthAndEastIsNorthEast() {
        assertEquals("north_east", MinecartTntAnimation.railShape(NORTH, CURRENT, EAST));
        assertEquals("north_east", MinecartTntAnimation.railShape(EAST, CURRENT, NORTH));
    }

    @Test
    void testTurnBetweenNorthAndWestIsNorthWest() {
        assertEquals("north_west", MinecartTntAnimation.railShape(NORTH, CURRENT, WEST));
    }

    @Test
    void testTurnBetweenSouthAndEastIsSouthEast() {
        assertEquals("south_east", MinecartTntAnimation.railShape(SOUTH, CURRENT, EAST));
    }

    @Test
    void testTurnBetweenSouthAndWestIsSouthWest() {
        assertEquals("south_west", MinecartTntAnimation.railShape(SOUTH, CURRENT, WEST));
    }
}
