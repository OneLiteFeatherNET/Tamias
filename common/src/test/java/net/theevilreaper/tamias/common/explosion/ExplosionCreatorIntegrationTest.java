package net.theevilreaper.tamias.common.explosion;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.particle.Particle;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MicrotusExtension.class)
class ExplosionCreatorIntegrationTest {

    @Test
    void testExplosionSupplierForTheGame(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        assertNull(instance.getExplosionSupplier());
        instance.setExplosionSupplier(new ExplosionCreator());
        assertNotNull(instance.getExplosionSupplier());

        TestConnection testConnection = env.createConnection();
        Pos pos = new Pos(100, 100, 100);
        Player player = testConnection.connect(instance, pos);

        assertEquals(instance, player.getInstance());
        assertEquals(pos, player.getPosition());
        Collector<ExplosionPacket> explosionTracker = testConnection.trackIncoming(ExplosionPacket.class);

        Pos blockPos = pos.sub(0, 1, 0);
        instance.setBlock(blockPos, Block.STONE);

        instance.explode((float) pos.x(), (float) pos.y(), (float) pos.z(), 1F);

        explosionTracker.assertCount(1);
        explosionTracker.assertSingle(packet -> {
            assertEquals(pos.asVec(), packet.center());
            assertEquals(Particle.EXPLOSION_EMITTER, packet.particle());
        });
        assertEquals(Block.STONE, instance.getBlock(blockPos), "The explosion should not destroy any blocks");

        env.destroyInstance(instance, true);
    }
}
