package net.theevilreaper.tamias.game.phase;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.common.DisconnectPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class RestartPhaseIntegrationTest {

    @Test
    void testRestartPhase(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection testConnection = env.createConnection();
        Player player = testConnection.connect(instance);

        RestartPhase restartPhase = new RestartPhase();
        assertNotNull(restartPhase);

        // Set the duration to 3 ticks for testing
        restartPhase.setCurrentTicks(3);

        restartPhase.start();
        assertTrue(restartPhase.isRunning(), "The RestartPhase should be running after starting");

        Collector<SystemChatPacket> messagePackets = testConnection.trackIncoming(SystemChatPacket.class);

        env.tick();
        assertEquals(2, restartPhase.getCurrentTicks(), "The current ticks should decrease after a tick");

        messagePackets.assertSingle();
        List<SystemChatPacket> trackedPackets = messagePackets.collect();

        SystemChatPacket timeMessagePacket = trackedPackets.getFirst();
        assertNotNull(timeMessagePacket);
        assertRawComponentContains("2 seconds!", timeMessagePacket.message());

        env.tickWhile(() -> true, Duration.ofSeconds(1));

        Collector<DisconnectPacket> disconnectTracker = testConnection.trackIncoming(DisconnectPacket.class);
        env.tickWhile(() -> true, Duration.ofSeconds(2));
        disconnectTracker.assertSingle();
        List<DisconnectPacket> disconnectPackets = disconnectTracker.collect();
        assertEquals(1, disconnectPackets.size(), "There should be one disconnect packet after the phase finishes");
        DisconnectPacket disconnectPacket = disconnectPackets.getFirst();
        assertNotNull(disconnectPacket, "The disconnect packet should not be null");
        assertRawComponentContains("The game is over. Thanks for playing it. <3", disconnectPacket.message());
        assertEquals(-1, restartPhase.getCurrentTicks(), "The current ticks should be -1 after the phase finishes");

        env.destroyInstance(instance, true);
        assertTrue(instance.getPlayers().isEmpty(), "The instance should have no players after destruction");
    }

    /**
     * Asserts that the raw text of a component contains the expected string.
     *
     * @param expected  the expected string to find in the raw text
     * @param component the component to check
     */
    private void assertRawComponentContains(@NotNull String expected, @NotNull Component component) {
        assertNotNull(component, "The component should not be null");
        assertNotEquals(Component.space(), component, "The component should not be empty");
        String rawText = PlainTextComponentSerializer.plainText().serialize(component);
        assertNotNull(rawText, "The raw text of the component should not be null");
        assertFalse(rawText.isEmpty(), "The raw text of the component should not be empty");
        assertTrue(rawText.contains(expected), "The raw text should contain: " + expected);
    }
}
