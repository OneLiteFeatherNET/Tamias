package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GroundFillPattern;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class SweepAreaPlacementIntegrationTest {

    @Test
    void testSweepAreaPlacementCompletion(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Vec groundPos = new Vec(1, 0, 1);
        Vec specialPos = new Vec(2, 0, 2);

        List<Vec> positions = List.of(groundPos, specialPos);
        List<Vec> specialPositions = List.of(specialPos);

        SweepAreaPlacement placement = new SweepAreaPlacement(instance, positions, specialPositions, GroundFillPattern.CIRCLE.comparator());
        assertFalse(placement.isRunning());

        GroundData groundData = new GroundData(Block.GRASS_BLOCK, List.of(Block.GOLD_BLOCK));

        FlexibleListener<AreaFinishBuildEvent> listener = env.listen(AreaFinishBuildEvent.class);

        placement.place(groundData);
        assertTrue(placement.isRunning());
        assertNotNull(placement.getTask());

        // Tick until placement completes and event is dispatched
        for (int i = 0; i < 10 && placement.isRunning(); i++) {
            env.tick();
        }

        assertFalse(placement.isRunning());
        assertNull(placement.getTask());

        assertEquals(Block.GRASS_BLOCK, instance.getBlock(groundPos));
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(specialPos));

        env.destroyInstance(instance);
    }

    @Test
    void testManualStop(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Vec pos = new Vec(1, 0, 1);

        SweepAreaPlacement placement = new SweepAreaPlacement(instance, List.of(pos), List.of(), GroundFillPattern.LINE.comparator());
        GroundData groundData = new GroundData(Block.DIRT, null);

        placement.place(groundData);
        assertTrue(placement.isRunning());

        placement.stop();
        assertFalse(placement.isRunning());
        assertNull(placement.getTask());

        env.destroyInstance(instance);
    }
}
