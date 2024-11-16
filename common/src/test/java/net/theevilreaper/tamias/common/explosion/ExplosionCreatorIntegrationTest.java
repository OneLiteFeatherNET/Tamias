package net.theevilreaper.tamias.common.explosion;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
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
        Vec vec = new Vec(100, 100, 100);
        Player player = testConnection.connect(instance, Pos.fromPoint(vec)).join();

        assertEquals(instance, player.getInstance());
        assertEquals(vec, player.getPosition().asVec());
        Collector<ExplosionPacket> explosionTracker = testConnection.trackIncoming(ExplosionPacket.class);

        instance.explode((float) vec.x(), (float) vec.y(), (float) vec.z(), 1F);

        explosionTracker.assertCount(1);
        explosionTracker.assertSingle(packet -> {
            assertEquals(1, packet.radius());
            assertEquals(ExplosionPacket.BlockInteraction.DESTROY, packet.blockInteraction());
        });

        env.destroyInstance(instance, true);
    }
}
