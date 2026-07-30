package net.theevilreaper.tamias.game.listener.area;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.area.placement.CircleAreaPlacement;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
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

        List<Vec> positions = gameArea.getPositions().stream().map(Vec.class::cast).toList();
        CircleAreaPlacement groundPlacement = new CircleAreaPlacement(instance, positions, new ArrayList<>());
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea, groundPlacement);

        for (Vec position : positions) {
            instance.setBlock(position, Block.STONE);
        }

        GameCleanupListener listener = new GameCleanupListener(gamePlacement);
        listener.accept(new GameCleanupEvent());

        for (Vec position : positions) {
            assertEquals(Block.AIR, instance.getBlock(position));
        }

        env.destroyInstance(instance);
    }
}
