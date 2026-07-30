package net.theevilreaper.tamias.game.listener.area;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.area.holder.GamePlacement;
import net.theevilreaper.tamias.common.area.placement.CircleAreaPlacement;
import net.theevilreaper.tamias.common.event.GameAreaChunksReadyEvent;
import net.theevilreaper.tamias.common.map.event.MapPrepareEvent;
import net.theevilreaper.tamias.common.map.layer.AreaData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class MapPrepareListenerTest {

    @Test
    void testChunkPreloadFiresGameAreaChunksReadyEvent(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        GameArea gameArea = new GameArea(
                AreaData.builder()
                        .lowerCorner(Vec.ZERO)
                        .upperCorner(new Vec(20, 0, 20))
                        .facing(Direction.NORTH)
                        .build()
        );
        gameArea.calculatePositions();
        CircleAreaPlacement groundPlacement = new CircleAreaPlacement(instance, new ArrayList<>(), new ArrayList<>());
        GamePlacement gamePlacement = new GamePlacement(instance, gameArea, groundPlacement);

        AtomicInteger firedCount = new AtomicInteger();
        env.process().eventHandler()
                .addListener(GameAreaChunksReadyEvent.class, event -> firedCount.incrementAndGet());

        MapPrepareListener listener = new MapPrepareListener(gamePlacement);
        listener.accept(new MapPrepareEvent());

        env.tickWhile(() -> firedCount.get() == 0, Duration.ofSeconds(10));

        assertEquals(1, firedCount.get());

        env.destroyInstance(instance);
    }
}
