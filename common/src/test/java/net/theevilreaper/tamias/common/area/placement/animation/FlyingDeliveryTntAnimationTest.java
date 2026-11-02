package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlyingDeliveryTntAnimationTest {

    private static final double DELTA = 1e-9;
    private static final int MAX_STEPS = 100;

    @Test
    void testNextPositionMovesBySpeedTowardsGoal() {
        Vec next = FlyingDeliveryTntAnimation.nextPosition(new Vec(0, 0, 0), new Vec(10, 0, 0), 1.0);

        assertEquals(1.0, next.x(), DELTA, "The carrier must move exactly the speed towards the goal on the x axis");
        assertEquals(0.0, next.y(), DELTA, "The carrier must not move on the y axis");
        assertEquals(0.0, next.z(), DELTA, "The carrier must not move on the z axis");
    }

    @Test
    void testNextPositionMovesBySpeedOnDiagonal() {
        Vec current = new Vec(0, 0, 0);
        Vec next = FlyingDeliveryTntAnimation.nextPosition(current, new Vec(3, 0, 4), 1.0);

        assertEquals(1.0, next.distance(current), DELTA, "The step length must be the speed, not the axis distance");
        assertEquals(0.6, next.x(), DELTA, "The step must point along the direction to the goal");
        assertEquals(0.8, next.z(), DELTA, "The step must point along the direction to the goal");
    }

    @Test
    void testNextPositionReturnsGoalWhenGoalIsWithinSpeed() {
        Vec goal = new Vec(0.5, 0, 0);

        Vec next = FlyingDeliveryTntAnimation.nextPosition(new Vec(0, 0, 0), goal, 1.0);

        assertEquals(goal, next, "A goal closer than the speed must be reached exactly, without overshooting");
    }

    @Test
    void testNextPositionReturnsGoalWhenAlreadyThere() {
        Vec goal = new Vec(2, 3, 4);

        Vec next = FlyingDeliveryTntAnimation.nextPosition(goal, goal, 1.0);

        assertEquals(goal, next, "A carrier which is at the goal must stay at the goal");
    }

    @Test
    void testNextPositionNeverOvershootsTheGoal() {
        Vec current = new Vec(0, 0, 0);
        Vec goal = new Vec(3, 4, 12);

        Vec next = FlyingDeliveryTntAnimation.nextPosition(current, goal, 1.0);

        assertTrue(next.distance(goal) < goal.distance(current),
                "A step towards the goal must get closer to the goal, but it was " + next.distance(goal) + " blocks away");
        assertTrue(next.distance(current) <= 1.0 + DELTA,
                "A step must not be longer than the speed, but it was " + next.distance(current) + " blocks");
    }

    @Test
    void testNextPositionArrivesAtGoalWithinBoundedSteps() {
        Vec goal = new Vec(3, 4, 12);
        Vec current = new Vec(0, 0, 0);

        int steps = 0;
        while (steps < MAX_STEPS && !current.equals(goal)) {
            current = FlyingDeliveryTntAnimation.nextPosition(current, goal, 1.0);
            steps++;
        }

        assertEquals(goal, current, "The carrier must arrive at the goal");
        assertTrue(steps <= 14, "A distance of 13 blocks at a speed of 1 must take about 13 ticks, but it took " + steps);
    }

    @Test
    void testSwingMovesParcelByFollowShareTowardsHangPoint() {
        Vec next = FlyingDeliveryTntAnimation.swing(new Vec(0, 0, 0), new Vec(10, 0, 0), 0.35);

        assertEquals(3.5, next.x(), DELTA, "The parcel must cover the follow share of the distance to the hanging point");
        assertEquals(0.0, next.y(), DELTA, "The parcel must not move on the y axis when the hanging point is level");
    }

    @Test
    void testSwingKeepsParcelWhenFollowIsZero() {
        Vec parcel = new Vec(1, 2, 3);

        Vec next = FlyingDeliveryTntAnimation.swing(parcel, new Vec(10, 10, 10), 0.0);

        assertEquals(parcel, next, "A parcel which does not follow must stay where it is");
    }

    @Test
    void testSwingReachesHangPointWhenFollowIsOne() {
        Vec hang = new Vec(4, 5, 6);

        Vec next = FlyingDeliveryTntAnimation.swing(new Vec(0, 0, 0), hang, 1.0);

        assertEquals(hang, next, "A parcel which follows fully must be at the hanging point");
    }

    @Test
    void testSwingTrailsBehindMovingHangPoint() {
        Vec parcel = new Vec(0, 0, 0);
        Vec hang = new Vec(10, 0, 0);

        Vec next = FlyingDeliveryTntAnimation.swing(parcel, hang, 0.35);

        assertTrue(next.x() < hang.x(), "The parcel must lag behind the hanging point, it was at x " + next.x());
    }

    @Test
    void testClampRopePullsParcelInsideMaxRopeKeepingItsDirection() {
        Vec carrier = new Vec(0, 10, 0);
        Vec parcel = new Vec(0, 6, 0);

        Vec clamped = FlyingDeliveryTntAnimation.clampRope(parcel, carrier, FlyingDeliveryTntAnimation.MAX_ROPE);

        assertEquals(FlyingDeliveryTntAnimation.MAX_ROPE, clamped.distance(carrier), DELTA,
                "A parcel further away than the rope must be pulled to the maximum rope length");
        assertEquals(10 - FlyingDeliveryTntAnimation.MAX_ROPE, clamped.y(), DELTA,
                "The parcel must stay below the carrier, on the same side as before");
    }

    @Test
    void testClampRopeKeepsParcelWhichIsWithinMaxRope() {
        Vec carrier = new Vec(0, 10, 0);
        Vec parcel = new Vec(0.5, 8.2, 0);

        Vec clamped = FlyingDeliveryTntAnimation.clampRope(parcel, carrier, FlyingDeliveryTntAnimation.MAX_ROPE);

        assertEquals(parcel, clamped, "A parcel within the rope length must not be moved");
    }

    @Test
    void testLegSpeedStartsBelowFlySpeed() {
        double speed = FlyingDeliveryTntAnimation.legSpeed(0, 100);

        assertTrue(speed < FlyingDeliveryTntAnimation.FLY_SPEED,
                "A leg must start slower than the fly speed, but it started at " + speed);
    }

    @Test
    void testLegSpeedRampsUpOverAccelTicks() {
        double previous = 0;
        for (int ticks = 0; ticks < FlyingDeliveryTntAnimation.ACCEL_TICKS; ticks++) {
            double speed = FlyingDeliveryTntAnimation.legSpeed(ticks, 100);
            assertTrue(speed >= previous, "The speed must not drop while accelerating, tick " + ticks + " was " + speed);
            previous = speed;
        }
        assertEquals(FlyingDeliveryTntAnimation.FLY_SPEED,
                FlyingDeliveryTntAnimation.legSpeed(FlyingDeliveryTntAnimation.ACCEL_TICKS - 1, 100), DELTA,
                "The full speed must be reached after the acceleration ticks");
    }

    @Test
    void testLegSpeedFliesAtFullSpeedAwayFromGoal() {
        assertEquals(FlyingDeliveryTntAnimation.FLY_SPEED,
                FlyingDeliveryTntAnimation.legSpeed(50, FlyingDeliveryTntAnimation.BRAKE_DISTANCE), DELTA,
                "A carrier at the brake distance must still fly at full speed");
    }

    @Test
    void testLegSpeedSlowsDownWithinBrakeDistance() {
        double speed = FlyingDeliveryTntAnimation.legSpeed(50, 1.5);

        assertEquals(FlyingDeliveryTntAnimation.FLY_SPEED * 0.5, speed, DELTA,
                "Half of the brake distance left must halve the speed");
    }

    @Test
    void testLegSpeedNeverDropsBelowMinimum() {
        double speed = FlyingDeliveryTntAnimation.legSpeed(50, 0.01);

        assertEquals(FlyingDeliveryTntAnimation.MIN_SPEED, speed, DELTA,
                "The speed near the goal must stay at the minimum so that the leg arrives");
    }

    @Test
    void testLegSpeedArrivesWithinBoundedSteps() {
        Vec goal = new Vec(0, 0, 13);
        Vec current = new Vec(0, 0, 0);

        int steps = 0;
        int ticksInLeg = 0;
        while (steps < MAX_STEPS && !current.equals(goal)) {
            current = FlyingDeliveryTntAnimation.nextPosition(current, goal,
                    FlyingDeliveryTntAnimation.legSpeed(ticksInLeg, current.distance(goal)));
            ticksInLeg++;
            steps++;
        }

        assertEquals(goal, current, "A leg with the speed curve must arrive at its goal");
        assertTrue(steps <= 40, "A leg of 13 blocks must arrive within 40 ticks, but it took " + steps);
    }

    @Test
    void testCarrierCountIsZeroWithoutPositions() {
        assertEquals(0, FlyingDeliveryTntAnimation.carrierCount(0), "No position must not spawn any carrier");
    }

    @Test
    void testCarrierCountNeverExceedsPositions() {
        assertEquals(2, FlyingDeliveryTntAnimation.carrierCount(2), "Two positions must not get more than two carriers");
        assertEquals(3, FlyingDeliveryTntAnimation.carrierCount(3), "Three positions must not get more than three carriers");
    }

    @Test
    void testCarrierCountIsMinimumForFewPositions() {
        assertEquals(FlyingDeliveryTntAnimation.MIN_CARRIERS, FlyingDeliveryTntAnimation.carrierCount(9),
                "Nine positions must use the minimum carrier count");
        assertEquals(FlyingDeliveryTntAnimation.MIN_CARRIERS, FlyingDeliveryTntAnimation.carrierCount(20),
                "Twenty positions must still use the minimum carrier count");
    }

    @Test
    void testCarrierCountGrowsWithPositions() {
        assertEquals(8, FlyingDeliveryTntAnimation.carrierCount(48), "Every sixth position must add a carrier");
        assertEquals(14, FlyingDeliveryTntAnimation.carrierCount(88), "A full round of TNT must get many carriers");
    }

    @Test
    void testCarrierCountIsCappedAtMaximum() {
        assertEquals(FlyingDeliveryTntAnimation.MAX_CARRIERS, FlyingDeliveryTntAnimation.carrierCount(96),
                "Many positions must not exceed the maximum carrier count");
        assertEquals(FlyingDeliveryTntAnimation.MAX_CARRIERS, FlyingDeliveryTntAnimation.carrierCount(400),
                "Very many positions must not exceed the maximum carrier count");
    }
}
