package net.theevilreaper.tamias.common.area.placement.animation;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class WallRouteIntegrationTest {

    private static final long SEED = 42L;
    private static final int MAX_TICKS = 2000;
    private static final int Y = 50;
    private static final Vec WEST_TARGET = new Vec(2, Y, 5);
    private static final Vec EAST_TARGET = new Vec(12, Y, 5);

    @Test
    void testVillagerNeverStandsInASolidCellWhileAWallLiesBetween(@NotNull Env env) {
        Instance instance = createInstanceWithWall(env);
        List<Vec> positions = List.of(WEST_TARGET, EAST_TARGET);
        List<Vec> landed = new ArrayList<>();

        TNTPlacement placement = new TNTPlacement(instance, landed::add);
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity villager : findEntities(instance, EntityType.VILLAGER)) {
                Vec cell = GridRoute.cellOf(villager.getPosition());
                assertFalse(instance.getBlock(cell).solid(), "A villager must never stand in a solid cell, it was at " + cell);
                assertFalse(instance.getBlock(cell.add(0, 1, 0)).solid(), "A villager must never stand in a solid cell, it was at " + cell);
            }
        }

        assertTrue(placement.landed().isDone(), "The run must complete around the wall");
        assertEquals(Block.TNT, instance.getBlock(EAST_TARGET), "The position behind the wall must still land");
        env.destroyInstance(instance);
    }

    @Test
    void testVillagerLandsAnEnclosedTargetWithoutARoute(@NotNull Env env) {
        Instance instance = createInstanceWithEnclosure(env);
        List<Vec> positions = List.of(WEST_TARGET, EAST_TARGET);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new VillagerTntAnimation(new Random(SEED)));
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
        }

        assertTrue(placement.landed().isDone(), "An enclosed target must not hang the run");
        assertEquals(Block.TNT, instance.getBlock(EAST_TARGET), "The enclosed target must be placed without a route");
        env.destroyInstance(instance);
    }

    @Test
    void testMinecartNeverDrivesIntoASolidCellWhileAWallLiesBetween(@NotNull Env env) {
        Instance instance = createInstanceWithWall(env);
        List<Vec> positions = List.of(WEST_TARGET, EAST_TARGET);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new MinecartTntAnimation());
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
            for (Entity cart : findEntities(instance, EntityType.TNT_MINECART)) {
                Vec cell = GridRoute.cellOf(cart.getPosition());
                assertFalse(instance.getBlock(cell).solid(), "A cart must never drive into a solid cell, it was at " + cell);
                assertFalse(instance.getBlock(cell.add(0, 1, 0)).solid(), "A cart must never drive into a solid cell, it was at " + cell);
            }
        }

        assertTrue(placement.landed().isDone(), "The run must complete around the wall");
        assertEquals(Block.TNT, instance.getBlock(EAST_TARGET), "The position behind the wall must still land");
        assertTrue(findEntities(instance, EntityType.TNT_MINECART).isEmpty(), "The cart must be removed at the end");
        env.destroyInstance(instance);
    }

    @Test
    void testMinecartLandsAnEnclosedTargetWithoutARoute(@NotNull Env env) {
        Instance instance = createInstanceWithEnclosure(env);
        List<Vec> positions = List.of(WEST_TARGET, EAST_TARGET);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(singleGroups(positions), _ -> true);
        placement.place(new MinecartTntAnimation());
        for (int i = 0; i < MAX_TICKS && !placement.landed().isDone(); i++) {
            env.tick();
        }

        assertTrue(placement.landed().isDone(), "An enclosed target must not hang the run");
        assertEquals(Block.TNT, instance.getBlock(EAST_TARGET), "The enclosed target must be placed without a route");
        env.destroyInstance(instance);
    }

    /**
     * Creates a flat instance with a stone wall at x = 8 which spans z from 0 to 10 and two blocks high.
     */
    private static Instance createInstanceWithWall(Env env) {
        Instance instance = createInstance(env);
        for (int z = 0; z <= 10; z++) {
            instance.setBlock(8, Y, z, Block.STONE);
            instance.setBlock(8, Y + 1, z, Block.STONE);
        }
        return instance;
    }

    /**
     * Creates a flat instance where the east target is surrounded by stone on all four sides.
     */
    private static Instance createInstanceWithEnclosure(Env env) {
        Instance instance = createInstance(env);
        instance.setBlock(11, Y, 5, Block.STONE);
        instance.setBlock(13, Y, 5, Block.STONE);
        instance.setBlock(12, Y, 4, Block.STONE);
        instance.setBlock(12, Y, 6, Block.STONE);
        return instance;
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        return instance;
    }

    private static List<TntGroup> singleGroups(List<Vec> positions) {
        return positions.stream().map(position -> new TntGroup(position, List.of())).toList();
    }

    private static List<Entity> findEntities(Instance instance, EntityType type) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == type)
                .toList();
    }
}
