package net.theevilreaper.tamias.game.listener.area;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.area.holder.SpawnPlacement;
import net.theevilreaper.tamias.common.event.SpawnCleanupEvent;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class SpawnCleanupListenerTest {

    @Test
    void testSpawnCleanupClearsBlocks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Pos.ZERO).join();
        env.createPlayer(instance);

        SpawnArea spawnArea = new SpawnArea(new SpawnLayer(Pos.ZERO, Direction.NORTH), 1);
        SpawnPlacement spawnPlacement = new SpawnPlacement(instance, spawnArea);
        spawnPlacement.triggerPlacement(new GroundData(Block.TNT, null));
        assertEquals(Block.TNT, instance.getBlock(Pos.ZERO));

        SpawnCleanupListener listener = new SpawnCleanupListener(spawnPlacement);
        listener.accept(new SpawnCleanupEvent());

        assertEquals(Block.AIR, instance.getBlock(Pos.ZERO));

        env.destroyInstance(instance, true);
    }
}
