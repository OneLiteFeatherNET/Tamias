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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The start cell of a run is the average of all positions. These tests place a wall on that average, so the start
 * must be moved to a passable cell.
 */
@ExtendWith(MicrotusExtension.class)
class MinecartSpawnInWallIntegrationTest {

    private static final int Y = 50;
    private static final Vec WEST = new Vec(4, Y, 5);
    private static final Vec EAST = new Vec(10, Y, 5);

    @Test
    void testMinecartStartsOutsideTheWallAtTheAveragePosition(@NotNull Env env) {
        Instance instance = createInstanceWithWallOnTheAverage(env);

        TNTPlacement placement = new TNTPlacement(instance, _ -> {});
        placement.updateGroups(List.of(new TntGroup(WEST, List.of()), new TntGroup(EAST, List.of())), _ -> true);
        placement.place(new MinecartTntAnimation());

        List<Entity> carts = findEntities(instance, EntityType.TNT_MINECART);
        assertEquals(1, carts.size(), "The cart must be spawned when the run starts");
        Vec start = GridRoute.cellOf(carts.getFirst().getPosition());
        assertFalse(instance.getBlock(start).solid(), "The cart must not start in a solid cell, it was at " + start);
        assertFalse(instance.getBlock(start.add(0, 1, 0)).solid(), "The cart must not start in a solid cell, it was at " + start);

        placement.stop();
        env.destroyInstance(instance);
    }

    /**
     * Creates a flat instance with a stone column on the average position of {@link #WEST} and {@link #EAST}.
     */
    private static Instance createInstanceWithWallOnTheAverage(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Vec.ZERO).join();
        instance.setBlock(7, Y, 5, Block.STONE);
        instance.setBlock(7, Y + 1, 5, Block.STONE);
        return instance;
    }

    private static List<Entity> findEntities(Instance instance, EntityType type) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == type)
                .toList();
    }
}
