package net.theevilreaper.tamias.common.area.placement.animation;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.placement.TNTPlacement;
import net.theevilreaper.tamias.common.area.placement.TntGroup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class GrowingTntAnimationIntegrationTest {

    private static final Vec CENTER = new Vec(5, 50, 5);
    private static final Vec MEMBER = new Vec(6, 50, 5);
    private static final double DELTA = 1e-4;

    @Test
    void testBeforeTheGrowthEndsOnlyAStartScaledDisplayExists(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new GrowingTntAnimation());
        env.tickWhile(() -> findDisplays(instance).isEmpty(), Duration.ofSeconds(5));

        List<Entity> displays = findDisplays(instance);
        assertEquals(1, displays.size(), "The center must appear as one block display");
        assertNear(GrowingTntAnimation.START_SCALE, scale(displays.getFirst()).x(), "The display must start small");
        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT block may exist before the growth ends");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testFirstLegOvershootsWithANegativeTranslation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new GrowingTntAnimation());
        env.tickWhile(() -> findDisplays(instance).isEmpty(), Duration.ofSeconds(5));

        for (int i = 0; i < GrowingTntAnimation.GROW_START_DELAY + 1; i++) {
            env.tick();
        }

        Entity display = findDisplays(instance).getFirst();
        assertNear(GrowingTntAnimation.OVERSHOOT_SCALE, scale(display).x(), "The first leg must overshoot the full size");
        assertNear((1 - GrowingTntAnimation.OVERSHOOT_SCALE) / 2, translation(display).x(), "The overshoot must stay centered");
        assertNear((1 - GrowingTntAnimation.OVERSHOOT_SCALE) / 2, translation(display).y(), "The overshoot must stay centered");
        assertNear((1 - GrowingTntAnimation.OVERSHOOT_SCALE) / 2, translation(display).z(), "The overshoot must stay centered");
        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT block may exist during the first leg");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testSecondLegSettlesToTheFullSizeWithoutTranslation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new GrowingTntAnimation());
        env.tickWhile(() -> findDisplays(instance).isEmpty(), Duration.ofSeconds(5));

        for (int i = 0; i < GrowingTntAnimation.GROW_START_DELAY + GrowingTntAnimation.GROW_TICKS + 1; i++) {
            env.tick();
        }

        Entity display = findDisplays(instance).getFirst();
        assertNear(1, scale(display).x(), "The second leg must settle to the full size");
        assertNear(1, scale(display).y(), "The second leg must settle to the full size");
        assertNear(1, scale(display).z(), "The second leg must settle to the full size");
        assertNear(0, translation(display).x(), "The full size must not be translated");
        assertNear(0, translation(display).y(), "The full size must not be translated");
        assertNear(0, translation(display).z(), "The full size must not be translated");
        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT block may exist during the second leg");

        placement.stop();
        env.destroyInstance(instance);
    }

    @Test
    void testTntLandsAfterBothLegsAndTheDisplayIsRemoved(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<Vec> landed = new ArrayList<>();
        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of())), _ -> true);
        placement.place(new GrowingTntAnimation());
        env.tickWhile(() -> instance.getBlock(CENTER) != Block.TNT, Duration.ofSeconds(5));

        assertEquals(List.of(CENTER), landed, "The landed position must be reported");
        assertTrue(findDisplays(instance).isEmpty(), "The display must be removed once the TNT block is placed");

        env.destroyInstance(instance);
    }

    @Test
    void testMembersLandAfterTheCenter(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<Vec> landed = new ArrayList<>();
        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(new GrowingTntAnimation());
        env.tickWhile(() -> instance.getBlock(CENTER) != Block.TNT, Duration.ofSeconds(5));

        assertEquals(Block.AIR, instance.getBlock(MEMBER), "A member must land after the center");
        env.tickWhile(() -> instance.getBlock(MEMBER) != Block.TNT, Duration.ofSeconds(5));

        assertEquals(List.of(CENTER, MEMBER), landed, "The center lands first, then the member");
        env.destroyInstance(instance);
    }

    @Test
    void testStopRemovesTheDisplaysAndPlacesNoBlock(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();

        List<Vec> landed = new ArrayList<>();
        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(List.of(new TntGroup(CENTER, List.of(MEMBER))), _ -> true);
        placement.place(new GrowingTntAnimation());
        env.tickWhile(() -> findDisplays(instance).isEmpty(), Duration.ofSeconds(5));
        for (int i = 0; i < 3; i++) {
            env.tick();
        }

        placement.stop();
        assertTrue(findDisplays(instance).isEmpty(), "The displays must be removed when the placement stops");

        for (int i = 0; i < 30; i++) {
            env.tick();
        }
        assertEquals(Block.AIR, instance.getBlock(CENTER), "No TNT may be placed after the stop");
        assertEquals(Block.AIR, instance.getBlock(MEMBER), "No TNT may be placed after the stop");
        assertTrue(landed.isEmpty(), "No landing may be reported after the stop");
        assertTrue(findDisplays(instance).isEmpty(), "No display may be spawned after the stop");

        env.destroyInstance(instance);
    }

    private static List<Entity> findDisplays(Instance instance) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.BLOCK_DISPLAY)
                .toList();
    }

    private static Vec scale(Entity display) {
        return ((BlockDisplayMeta) display.getEntityMeta()).getScale();
    }

    private static Point translation(Entity display) {
        return ((BlockDisplayMeta) display.getEntityMeta()).getTranslation();
    }

    private static void assertNear(double expected, double actual, String message) {
        assertEquals(expected, actual, DELTA, message);
    }
}
