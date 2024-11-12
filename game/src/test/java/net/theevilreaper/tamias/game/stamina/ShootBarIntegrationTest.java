package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.game.stamina.StaminaBar.Status;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ShootBarIntegrationTest {

    @Test
    void testShootBarSingleShootFlow(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        StaminaBar shootBar = StaminaFactory.createShootBar(player);

        assertInstanceOf(ShootBar.class, shootBar);

        shootBar.start();

        assertEquals(Status.READY, shootBar.status);

        shootBar.triggerAction();

        assertNotEquals(Status.READY, shootBar.status);
        assertEquals(Status.REGENERATING, shootBar.status);

        shootBar.stop();

        assertNull(shootBar.status);

        env.destroyInstance(instance, true);

    }

    @Test
    void testOnRegeneratedSoundPlay(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO).join();

        env.destroyInstance(instance, true);

        StaminaBar shootBar = StaminaFactory.createShootBar(player);

        assertInstanceOf(ShootBar.class, shootBar);
        assertNotNull(shootBar);

        Collector<SoundEffectPacket> soundTracker = connection.trackIncoming(SoundEffectPacket.class);

        shootBar.onRegenerated();

        soundTracker.assertSingle();
        soundTracker.assertSingle(this::assertSoundLevelUp);
        assertEquals(Status.READY, shootBar.status);

        env.destroyInstance(instance, true);
    }

    @Test
    void testRegenerateFlow(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO).join();

        StaminaBar shootBar = StaminaFactory.createShootBar(player);

        // The test runs the logic manually, so the status is null in this case
        assertNull(shootBar.status);
        shootBar.consume();
        assertNull(shootBar.status);

        shootBar.triggerAction();
        assertEquals(Status.REGENERATING, shootBar.status);

        int waitingTicks = 0;
        while (waitingTicks < 16) {
            shootBar.consume();
            assertEquals(Status.REGENERATING, shootBar.status);
            ++waitingTicks;
        }

        assertEquals(Status.REGENERATING, shootBar.status);

        Collector<SoundEffectPacket> soundTracker = connection.trackIncoming(SoundEffectPacket.class);

        // The status should be ready after the 16 ticks
        shootBar.consume();

        soundTracker.assertSingle();
        soundTracker.assertSingle(this::assertSoundLevelUp);
        assertEquals(16, waitingTicks);
        assertEquals(Status.READY, shootBar.status);
    }

    /**
     * Asserts the sound packet for the level up sound.
     *
     * @param packet the packet to check
     */
    private void assertSoundLevelUp(@NotNull SoundEffectPacket packet) {
        assertInstanceOf(SoundEffectPacket.class, packet);
        assertEquals(SoundEvent.ENTITY_PLAYER_LEVELUP, packet.soundEvent());
        assertEquals(1.0f, packet.volume());
        assertEquals(1.0f, packet.pitch());
        assertEquals(Sound.Source.MASTER, packet.source());
    }
}
