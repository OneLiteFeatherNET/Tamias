package net.theevilreaper.tamias.game.listener.area;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class GameCleanupListenerTest {

    @Test
    void testGameCleanupClearsBlocks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(1, 0, 1))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();

        List<Vec> positions = List.copyOf(gameArea.getPositions());
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);

        for (Vec position : positions) {
            instance.setBlock(position, Block.STONE);
        }

        GameCleanupListener listener = new GameCleanupListener(gamePlacement, () -> false);
        listener.accept(new GameCleanupEvent());
        // The blocks are cleared asynchronously by a block batch
        Vec lastPosition = positions.getLast();
        env.tickWhile(() -> instance.getBlock(lastPosition) != Block.AIR, Duration.ofSeconds(5));

        for (Vec position : positions) {
            assertEquals(Block.AIR, instance.getBlock(position));
        }

        env.destroyInstance(instance);
    }

    @Test
    void testGameCleanupKeepsBlocksAfterLastRound(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(1, 0, 1))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();

        List<Vec> positions = List.copyOf(gameArea.getPositions());
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea);

        for (Vec position : positions) {
            instance.setBlock(position, Block.STONE);
        }

        GameCleanupListener listener = new GameCleanupListener(gamePlacement, () -> true);
        listener.accept(new GameCleanupEvent());
        env.tick();

        for (Vec position : positions) {
            assertEquals(Block.STONE, instance.getBlock(position));
        }

        env.destroyInstance(instance);
    }
}
