package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static net.theevilreaper.tamias.BlockAssertions.assertBlock;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TNTPlacementIntegrationTest {

    @Test
    void testTNTPlacement(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Vec start = Vec.ZERO;
        Vec end = new Vec(5, 0, 5);

        instance.loadChunk(start).join();
        instance.loadChunk(end).join();
        instance.setGenerator(unit -> unit.modifier().fill(start, end, Block.STONE));

        List<Vec> positions = new ArrayList<>();

        for (int x = start.blockX(); x <= end.blockX(); x++) {
            for (int z = start.blockZ(); z <= end.blockZ(); z++) {
                Vec pos = new Vec(x, 0, z);
                positions.add(pos);
                instance.setBlock(pos, Block.STONE);
            }
        }
        assertBlock(instance, positions, Block.STONE);

        List<Vec> testBlocks = List.of(start, end);
        TNTPlacement placement = new TNTPlacement(instance, testBlocks);
        assertNotNull(placement);
        assertInstanceOf(AreaBasePlacement.class, placement);

        placement.place(GroundDataRegistry.DEFAULT_SPAWN_DATA);
        assertTrue(placement.isRunning());

        for (int i = 0; i < 20; i++) {
            env.tick();
        }

        assertBlock(instance, testBlocks, Block.TNT);

        // Advance ticks so iterator finishes and placement stops
        for (int i = 0; i < 10 && placement.isRunning(); i++) {
            env.tick();
        }

        assertFalse(placement.isRunning());

        env.destroyInstance(instance);
    }
}
