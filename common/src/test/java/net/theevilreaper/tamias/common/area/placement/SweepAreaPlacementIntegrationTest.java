package net.theevilreaper.tamias.common.area.placement;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GroundFillPattern;
import net.theevilreaper.tamias.common.event.AreaBuildProgressEvent;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.ground.GroundData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
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

        SweepAreaPlacement placement = new SweepAreaPlacement(instance, positions, specialPositions);
        assertFalse(placement.isRunning());

        GroundData groundData = new GroundData(Block.GRASS_BLOCK, List.of(Block.GOLD_BLOCK));

        FlexibleListener<AreaFinishBuildEvent> listener = env.listen(AreaFinishBuildEvent.class);

        placement.place(groundData, GroundFillPattern.CIRCLE.comparator(Vec.ZERO));
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
    void testProgressIsReportedUntilComplete(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        List<Vec> positions = new ArrayList<>();
        for (int x = 0; x < 50; x++) {
            positions.add(new Vec(x, 0, 0));
        }
        List<Double> progress = new ArrayList<>();
        env.process().eventHandler().addListener(AreaBuildProgressEvent.class, event -> progress.add(event.progress()));
        SweepAreaPlacement placement = new SweepAreaPlacement(instance, positions, List.of());

        placement.place(new GroundData(Block.DIRT, null), GroundFillPattern.LINE.comparator(Vec.ZERO));
        for (int i = 0; i < 10 && placement.isRunning(); i++) {
            env.tick();
        }

        assertFalse(placement.isRunning());
        assertEquals(3, progress.size(), "50 blocks are placed in three steps of up to 20 blocks");
        assertEquals(0.4, progress.getFirst(), 1e-9);
        assertEquals(1.0, progress.getLast(), 1e-9);

        env.destroyInstance(instance);
    }

    @Test
    void testManualStop(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Vec pos = new Vec(1, 0, 1);

        SweepAreaPlacement placement = new SweepAreaPlacement(instance, List.of(pos), List.of());
        GroundData groundData = new GroundData(Block.DIRT, null);

        placement.place(groundData, GroundFillPattern.LINE.comparator(Vec.ZERO));
        assertTrue(placement.isRunning());

        placement.stop();
        assertFalse(placement.isRunning());
        assertNull(placement.getTask());

        env.destroyInstance(instance);
    }
}
