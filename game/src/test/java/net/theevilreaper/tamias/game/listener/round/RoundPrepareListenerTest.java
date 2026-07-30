package net.theevilreaper.tamias.game.listener.round;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.area.SpawnArea;
import net.theevilreaper.tamias.common.map.layer.SpawnLayer;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class RoundPrepareListenerTest {

    @Test
    void testOnlinePlayersTeleportedToSpawn(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(Pos.ZERO).join();
        Player player = env.createPlayer(instance);

        SpawnArea spawnArea = new SpawnArea(new SpawnLayer(Pos.ZERO, Direction.NORTH), 1);
        RoundPrepareListener listener = new RoundPrepareListener(spawnArea, instance);

        listener.accept(new RoundPrepareEvent());
        env.tick();

        Pos expected = Pos.ZERO.add(0.5, 1, 0.5);
        assertEquals(expected.blockX(), player.getPosition().blockX());
        assertEquals(expected.blockY(), player.getPosition().blockY());
        assertEquals(expected.blockZ(), player.getPosition().blockZ());

        env.destroyInstance(instance, true);
    }
}
