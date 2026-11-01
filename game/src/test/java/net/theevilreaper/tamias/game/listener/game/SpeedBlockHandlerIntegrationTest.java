package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.SpeedBlock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class SpeedBlockHandlerIntegrationTest {

    private static final GroundData WITH_SPEED = new GroundData(Block.PRISMARINE_BRICKS, List.of(SpeedBlock.of(Block.SEA_LANTERN)));
    private static final GroundData WITHOUT_SPEED = new GroundData(Block.SANDSTONE, List.of(Block.CUT_SANDSTONE));

    @Test
    void testListenerOnlyRegisteredWithSpeedBlocks(Env env) {
        EventNode<Event> node = EventNode.all("speed-test");
        AtomicInteger calls = new AtomicInteger();
        SpeedBlockHandler handler = new SpeedBlockHandler(node, _ -> calls.incrementAndGet());
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        handler.enable(WITHOUT_SPEED);
        handler.enable(null);
        assertFalse(handler.isActive());
        node.call(new PlayerMoveEvent(player, Pos.ZERO, true));
        assertEquals(0, calls.get());

        handler.enable(WITH_SPEED);
        // A second enable must not register the listener twice
        handler.enable(WITH_SPEED);
        assertTrue(handler.isActive());
        node.call(new PlayerMoveEvent(player, Pos.ZERO, true));
        assertEquals(1, calls.get());

        handler.disable();
        handler.disable();
        assertFalse(handler.isActive());
        node.call(new PlayerMoveEvent(player, Pos.ZERO, true));
        assertEquals(1, calls.get());

        env.destroyInstance(instance, true);
    }
}
