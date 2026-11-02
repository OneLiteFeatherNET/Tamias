package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.placement.TNTPlacement;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class FlyingDeliveryTntAnimationIntegrationTest {

    private static final int MAX_TICKS = 2000;
    private static final double Y = 50;
    private static final double DELTA = 1e-6;
    /**
     * The horizontal speed in blocks per tick above which a carrier counts as flying level.
     */
    private static final double LEVEL_SPEED = 0.1;

    static Stream<EntityType> carrierTypes() {
        return Stream.of(EntityType.PARROT, EntityType.BAT, EntityType.PHANTOM);
    }

    @ParameterizedTest
    @MethodSource("carrierTypes")
    void testSpawnsMinimumCarrierCountOfTheGivenType(EntityType carrierType, @NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(row(9)), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(carrierType));
        tick(env, 2);

        assertEquals(FlyingDeliveryTntAnimation.MIN_CARRIERS, findEntities(instance, carrierType).size(),
                "Nine positions must spawn the minimum carrier count of the given type");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testSpawnsOneCarrierPerPositionWhenThereAreFewerPositions(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        // The ground stays unready, so the carriers only bob at the depot
        placement.updateGroups(singleGroups(row(2)), _ -> false);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.PARROT));
        tick(env, 2);

        assertEquals(2, findEntities(instance, EntityType.PARROT).size(),
                "The run must not spawn more carriers than positions");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testNonReadyPositionsStayEmpty(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(3);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), _ -> false);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.BAT));
        tick(env, 60);

        assertTrue(landed.isEmpty(), "No position may land while its ground is not ready");
        assertEquals(0, countTnt(instance, positions), "No TNT block may be placed while the ground is not ready");
        assertTrue(findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(),
                "No TNT may be picked up while there is no open position");

        placement.stop();
        env.destroyInstance(instance);
    }

    @ParameterizedTest
    @MethodSource("carrierTypes")
    void testAllPositionsLandAndTheRunCompletes(EntityType carrierType, @NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(7);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(mixedGroups(positions), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(carrierType));
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(placement.landed().isDone(), "The run must complete once every position has landed");
        assertEquals(Set.copyOf(positions), Set.copyOf(landed), "Every position must be reported as landed");
        assertEquals(positions.size(), landed.size(), "No position may land twice");

        env.destroyInstance(instance);
    }

    @Test
    void testCarrierTakesTheNextPositionWithoutFlyingBackToTheDepot(@NotNull Env env) {
        Instance instance = createInstance(env);
        // More positions than carriers, so a carrier has to deliver a second time
        List<Vec> positions = row(FlyingDeliveryTntAnimation.MIN_CARRIERS + 1);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.PARROT));

        // A carrier at the depot would hold its TNT block above the cruise height
        double highestParcel = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            if (landed.isEmpty()) continue;
            for (Entity parcel : findEntities(instance, EntityType.BLOCK_DISPLAY)) {
                highestParcel = Math.max(highestParcel, parcel.getPosition().y());
            }
        }

        assertTrue(placement.landed().isDone(), "The run must complete");
        assertTrue(highestParcel > Double.NEGATIVE_INFINITY, "A second delivery must have been carried");
        assertTrue(highestParcel <= Y + FlyingDeliveryTntAnimation.CRUISE_HEIGHT,
                "The second TNT block must be picked up above the last position, not at the depot, but was at " + highestParcel);

        env.destroyInstance(instance);
    }

    @Test
    void testParcelIsRemovedWhenItsBlockLands(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(row(1)), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.PARROT));
        tickUntil(env, () -> !landed.isEmpty(), MAX_TICKS);

        assertEquals(1, landed.size(), "The single position must have landed");
        assertTrue(findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(),
                "The carried TNT block must be removed when its block lands");

        env.destroyInstance(instance);
    }

    @ParameterizedTest
    @MethodSource("carrierTypes")
    void testCarriersAreRemovedAfterTheLastLanding(EntityType carrierType, @NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(4);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(carrierType));
        tickUntil(env, () -> placement.landed().isDone(), MAX_TICKS);

        assertTrue(findEntities(instance, carrierType).isEmpty(),
                "The carriers must be removed after the last landing");
        assertTrue(findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(),
                "No carried TNT block may remain after the last landing");

        env.destroyInstance(instance);
    }

    @ParameterizedTest
    @MethodSource("carrierTypes")
    void testCarrierIsLeashedToItsParcelWhileCarrying(EntityType carrierType, @NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(row(1)), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(carrierType));
        tickUntil(env, () -> !findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(), MAX_TICKS);

        List<Entity> parcels = findEntities(instance, EntityType.BLOCK_DISPLAY);
        List<Entity> carriers = findEntities(instance, carrierType);
        assertEquals(1, parcels.size(), "The carrier must carry exactly one TNT block");
        assertEquals(1, carriers.size(), "One position must have exactly one carrier");
        assertSame(parcels.getFirst(), carriers.getFirst().getLeashHolder(),
                "The carrier must be leashed to the TNT block it carries");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testLeashIsRemovedWhenItsParcelLands(@NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(row(2)), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.PARROT));
        tickUntil(env, () -> !landed.isEmpty(), MAX_TICKS);

        assertFalse(landed.isEmpty(), "One position must have landed before the check");
        List<Entity> carriers = findEntities(instance, EntityType.PARROT);
        List<Entity> parcels = findEntities(instance, EntityType.BLOCK_DISPLAY);
        long leashed = carriers.stream().filter(carrier -> carrier.getLeashHolder() != null).count();
        assertEquals(parcels.size(), leashed,
                "Only the carriers which still carry a TNT block may be leashed");
        for (Entity carrier : carriers) {
            Entity holder = carrier.getLeashHolder();
            if (holder != null) {
                assertTrue(parcels.contains(holder), "A leash may only point to a TNT block which still exists");
            }
        }

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testParcelTrailsCarrierDuringLevelFlight(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(farPositions()), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.PARROT));

        Map<Entity, Vec> previous = new HashMap<>();
        boolean trailed = false;
        for (int i = 0; i < MAX_TICKS && !trailed; i++) {
            env.tick();
            for (Entity carrier : findEntities(instance, EntityType.PARROT)) {
                Vec now = Vec.fromPoint(carrier.getPosition());
                Vec before = previous.put(carrier, now);
                Entity parcel = carrier.getLeashHolder();
                if (parcel == null || before == null) continue;

                Vec motion = horizontal(now.sub(before));
                Vec lag = horizontal(Vec.fromPoint(parcel.getPosition()).sub(now));
                double alongMotion = lag.x() * motion.x() + lag.z() * motion.z();
                if (motion.length() > LEVEL_SPEED && lag.length() > LEVEL_SPEED && alongMotion < 0) {
                    trailed = true;
                }
            }
        }

        assertTrue(trailed, "While flying level the TNT block must trail behind the carrier on its rope");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testParcelStaysWithinRopeLength(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(farPositions()), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.BAT));

        int sampled = 0;
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity carrier : findEntities(instance, EntityType.BAT)) {
                Entity parcel = carrier.getLeashHolder();
                if (parcel == null) continue;
                sampled++;
                double distance = carrier.getPosition().distance(parcel.getPosition());
                assertTrue(distance <= FlyingDeliveryTntAnimation.MAX_ROPE + DELTA,
                        "The TNT block must never be further away than the rope allows, but it was " + distance);
            }
        }

        assertTrue(sampled > 0, "The run must carry a TNT block while the rope is checked");
        assertTrue(placement.landed().isDone(), "The run must complete within the tick limit");

        env.destroyInstance(instance);
    }

    @Test
    void testCarrierFliesLevelAtCruiseHeightWhileCarrying(@NotNull Env env) {
        Instance instance = createInstance(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(farPositions()), _ -> true);
        placement.place(new FlyingDeliveryTntAnimation(EntityType.PHANTOM));

        double cruiseY = Y + FlyingDeliveryTntAnimation.CRUISE_HEIGHT;
        Map<Entity, Vec> previous = new HashMap<>();
        int sampled = 0;
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity carrier : findEntities(instance, EntityType.PHANTOM)) {
                Vec now = Vec.fromPoint(carrier.getPosition());
                Vec before = previous.put(carrier, now);
                if (carrier.getLeashHolder() == null || before == null) continue;

                if (horizontal(now.sub(before)).length() <= LEVEL_SPEED) continue;
                sampled++;
                assertTrue(now.y() >= cruiseY - DELTA,
                        "A carrier which flies level with TNT must stay at the cruise height " + cruiseY
                                + ", but it was at " + now.y());
            }
        }

        assertTrue(sampled > 0, "The run must fly a level leg with TNT while the cruise height is checked");

        env.destroyInstance(instance);
    }

    @ParameterizedTest
    @MethodSource("carrierTypes")
    void testStopRemovesCarriersAndParcelsAndPlacesNothingAfterwards(EntityType carrierType, @NotNull Env env) {
        Instance instance = createInstance(env);
        List<Vec> positions = row(6);
        Set<Point> readyGround = ConcurrentHashMap.newKeySet();
        readyGround.add(positions.get(0).sub(0, 1, 0));
        readyGround.add(positions.get(1).sub(0, 1, 0));
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), readyGround::contains);
        placement.place(new FlyingDeliveryTntAnimation(carrierType));
        tickUntil(env, () -> landed.size() == 2, MAX_TICKS);
        assertEquals(2, landed.size(), "Two positions must have landed before the stop");
        assertFalse(findEntities(instance, carrierType).isEmpty(), "The carriers must still be working before the stop");

        placement.stop();
        int landedAfterStop = landed.size();
        assertTrue(findEntities(instance, carrierType).isEmpty(), "The stop must remove the carriers");
        assertTrue(findEntities(instance, EntityType.BLOCK_DISPLAY).isEmpty(), "The stop must remove the carried TNT");
        int tntAfterStop = countTnt(instance, positions);

        tick(env, 60);

        assertEquals(tntAfterStop, countTnt(instance, positions), "The block count must not change after the stop");
        assertEquals(landedAfterStop, landed.size(), "No landing may be reported after the stop");
        assertTrue(findEntities(instance, carrierType).isEmpty(), "No carrier may be spawned after the stop");
        env.destroyInstance(instance);
    }

    /**
     * Creates the instance with the chunks of the positions loaded, so a teleport of a carrier or its TNT block is
     * applied in the same tick and never waits for a chunk.
     */
    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        instance.loadChunk(new Vec(16, Y, 0)).join();
        return instance;
    }

    /**
     * Returns the positions in one row, the rows are spaced by one block so the carriers never overlap the positions.
     */
    private static List<Vec> row(int count) {
        List<Vec> positions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            positions.add(new Vec(1 + i, Y, 5));
        }
        return positions;
    }

    /**
     * Returns two positions far apart, so the carriers have to fly level from the depot to the positions.
     */
    private static List<Vec> farPositions() {
        return List.of(new Vec(1, Y, 5), new Vec(21, Y, 5));
    }

    private static List<TntGroup> singleGroups(List<Vec> positions) {
        return positions.stream().map(position -> new TntGroup(position, List.of())).toList();
    }

    /**
     * Returns one group with a center and two members, followed by single positions for the rest.
     */
    private static List<TntGroup> mixedGroups(List<Vec> positions) {
        List<TntGroup> groups = new ArrayList<>();
        groups.add(new TntGroup(positions.get(0), List.of(positions.get(1), positions.get(2))));
        for (Vec position : positions.subList(3, positions.size())) {
            groups.add(new TntGroup(position, List.of()));
        }
        return groups;
    }

    /**
     * Returns the horizontal part of the vector, the y component is set to zero.
     */
    private static Vec horizontal(Vec vector) {
        return new Vec(vector.x(), 0, vector.z());
    }

    private static List<Entity> findEntities(Instance instance, EntityType type) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == type)
                .toList();
    }

    private static int countTnt(Instance instance, List<Vec> positions) {
        int count = 0;
        for (Vec position : positions) {
            if (instance.getBlock(position) == Block.TNT) {
                count++;
            }
        }
        return count;
    }

    private static void tick(Env env, int ticks) {
        for (int i = 0; i < ticks; i++) {
            env.tick();
        }
    }

    private static void tickUntil(Env env, BooleanSupplier condition, int maxTicks) {
        for (int i = 0; i < maxTicks && !condition.getAsBoolean(); i++) {
            env.tick();
        }
    }
}
