package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShootingTntAnimationPathTest {

    private static final Vec LAUNCH = new Vec(0, 51, 0);
    private static final Vec TARGET = new Vec(10, 50, 0);
    private static final double HEIGHT = 3;
    private static final int FLIGHT_TICKS = 20;
    private static final double DELTA = 1e-9;

    @Test
    void testPathStartsAtTheLaunchPoint() {
        Vec start = ShootingTntAnimation.pointOnPath(LAUNCH, TARGET, HEIGHT, 0, FLIGHT_TICKS);

        assertPointEquals(LAUNCH, start, "The path must start at the launch point");
    }

    @Test
    void testPathEndsAtTheTarget() {
        Vec end = ShootingTntAnimation.pointOnPath(LAUNCH, TARGET, HEIGHT, FLIGHT_TICKS, FLIGHT_TICKS);

        assertPointEquals(TARGET, end, "The path must end at the target");
    }

    @Test
    void testPeakIsTheArcHeightAboveTheLinearMidpoint() {
        Vec peak = ShootingTntAnimation.pointOnPath(LAUNCH, TARGET, HEIGHT, FLIGHT_TICKS / 2, FLIGHT_TICKS);

        double linearMidpointY = (LAUNCH.y() + TARGET.y()) / 2;
        assertEquals(linearMidpointY + HEIGHT, peak.y(), DELTA, "The peak must be the arc height above the straight line");
        assertTrue(peak.y() > LAUNCH.y() && peak.y() > TARGET.y(), "The peak must be above both ends of the flight");
    }

    @Test
    void testFlightTicksAreClampedToTheMinimum() {
        assertEquals(10, ShootingTntAnimation.flightTicks(0), "A very short shot must fly the minimum ticks");
        assertEquals(10, ShootingTntAnimation.flightTicks(3), "A short shot must fly the minimum ticks");
    }

    @Test
    void testFlightTicksAreClampedToTheMaximum() {
        assertEquals(40, ShootingTntAnimation.flightTicks(1000), "A very long shot must fly the maximum ticks");
    }

    @Test
    void testFlightTicksFollowTheDistance() {
        assertEquals(20, ShootingTntAnimation.flightTicks(14), "The flight ticks are the distance divided by the speed");
    }

    @Test
    void testArcHeightHasAMinimum() {
        assertEquals(ShootingTntAnimation.MIN_ARC, ShootingTntAnimation.arcHeight(1), DELTA, "A short shot must still arc");
    }

    @Test
    void testArcHeightGrowsWithTheDistance() {
        assertEquals(30, ShootingTntAnimation.arcHeight(100), DELTA, "A long shot must arc higher");
    }

    private static void assertPointEquals(Vec expected, Vec actual, String message) {
        assertEquals(expected.x(), actual.x(), DELTA, message);
        assertEquals(expected.y(), actual.y(), DELTA, message);
        assertEquals(expected.z(), actual.z(), DELTA, message);
    }
}
