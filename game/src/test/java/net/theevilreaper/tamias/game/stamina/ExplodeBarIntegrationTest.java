package net.theevilreaper.tamias.game.stamina;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.FlexibleListener;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.stamina.StaminaBar.Status;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ExplodeBarIntegrationTest {

    @Test
    void testExplodeBarStartFlow(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        StaminaBar explodeBar = StaminaFactory.createExplodeBar(player);

        assertInstanceOf(ExplodeBar.class, explodeBar);
        assertNull(explodeBar.status);

        explodeBar.onStart();

        assertNotNull(explodeBar.status);
        assertEquals(Status.READY, explodeBar.status);

        assertEquals(10, player.getLevel());
        assertEquals(1f, player.getExp());

        env.destroyInstance(instance, true);
    }

    @Test
    void testTriggerActionMethod(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        StaminaBar explodeBar = StaminaFactory.createExplodeBar(player);

        assertInstanceOf(ExplodeBar.class, explodeBar);
        assertNull(explodeBar.status);

        explodeBar.triggerAction();

        assertNotNull(explodeBar.status);
        assertEquals(Status.DRAINING, explodeBar.status);

        env.destroyInstance(instance, true);
    }

    @Test
    void testConsumeFlow(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO);

        StaminaBar explodeBar = StaminaFactory.createExplodeBar(player);

        assertInstanceOf(ExplodeBar.class, explodeBar);

        // Not started yet, so no time should be consumed
        assertNull(explodeBar.status);
        explodeBar.consume();
        assertNull(explodeBar.status);

        // Start the bar, also no time should be consumed
        explodeBar.onStart();
        assertNotNull(explodeBar.status);
        assertEquals(Status.READY, explodeBar.status);
        explodeBar.consume();
        assertEquals(Status.READY, explodeBar.status);

        // Trigger the action, time should be consumed
        explodeBar.triggerAction();
        assertEquals(Status.DRAINING, explodeBar.status);
        assertNotEquals(Status.REGENERATING, explodeBar.status);

        float level = player.getLevel() - 1;

        Collector<SoundEffectPacket> soundTracker = connection.trackIncoming(SoundEffectPacket.class);

        for (int i = 0; i <= 10; i++) {
            explodeBar.consume();
            assertBomberTick(level, player);
            soundTracker.assertSingle();
            soundTracker.assertSingle(this::assertTickSound);
            --level;
        }

        // Do last tick
        explodeBar.consume();

        // TODO: Check why the sound is wrong here
        // soundTracker.assertSingle();
        // soundTracker.assertSingle(this::assertExplodeSound);
        assertEquals(Status.REGENERATING, explodeBar.status);

        FlexibleListener<BomberRequireSpawnEvent> eventListener = env.listen(BomberRequireSpawnEvent.class);
        eventListener.followup(event -> {
            assertEquals(player, event.getPlayer());
            assertEquals(explodeBar, event.getExplodeBar());
            // TODO: Fix me later
            //assertTrue(player.hasEffect(PotionEffect.BLINDNESS));
            assertEquals(0, player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue());
        });
        for (int i = 0; i < 5; i++) {
            explodeBar.consume();
            assertEquals(Status.REGENERATING, explodeBar.status);
        }

        env.destroyInstance(instance, true);
    }

    /**
     * Asserts the bomber tick for the player.
     *
     * @param level  the expected level
     * @param player the player to check
     */
    private void assertBomberTick(float level, @NotNull Player player) {
        assertEquals(level, player.getLevel());
        assertTrue(player.getExp() >= 0 && player.getExp() <= 1);
    }

    /**
     * Asserts the explode sound for the bomber.
     *
     * @param packet the packet to check
     */
    private void assertExplodeSound(@NotNull SoundEffectPacket packet) {
        System.out.println("Sound: " + packet.soundEvent());
        assertEquals(SoundEvent.ENTITY_GENERIC_EXPLODE, packet.soundEvent());
        assertEquals(1.0f, packet.volume());
        assertEquals(0f, packet.pitch());
        assertEquals(Sound.Source.MASTER, packet.source());
    }

    /**
     * Asserts the tick sound for the bomber.
     *
     * @param packet the packet to check
     */
    private void assertTickSound(@NotNull SoundEffectPacket packet) {
        assertEquals(SoundEvent.UI_BUTTON_CLICK, packet.soundEvent());
        assertEquals(1.0f, packet.volume());
        assertEquals(10f, packet.pitch());
        assertEquals(Sound.Source.MASTER, packet.source());
    }
}
