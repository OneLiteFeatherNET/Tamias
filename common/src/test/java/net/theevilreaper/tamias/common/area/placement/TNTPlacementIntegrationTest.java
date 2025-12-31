package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.placement.types.TNTPlacement;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static net.theevilreaper.tamias.BlockAssertions.assertBlock;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MicrotusExtension.class)
class TNTPlacementIntegrationTest {

    @Disabled("Should be fixed in the near future")
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
                positions.add(new Vec(x, 0, z));
            }
        }
        assertBlock(instance, positions, Block.STONE);

        List<Vec> testBlocks = List.of(start.add(0, 1, 0), end.add(0, 1, 0));
        TNTPlacement placement = new TNTPlacement(instance, testBlocks);
        assertNotNull(placement);
        assertInstanceOf(AreaBasePlacement.class, placement);

        placement.place(GroundDataRegistry.DEFAULT_SPAWN_DATA);

        for (int i = 0; i < 50_000; i++) {
            env.tick();
        }

        assertBlock(instance, testBlocks, Block.TNT);
        env.destroyInstance(instance);
    }
}
