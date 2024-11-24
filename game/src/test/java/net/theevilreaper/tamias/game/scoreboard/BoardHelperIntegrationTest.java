package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class BoardHelperIntegrationTest {

    @Disabled
    @Test
    void testLobbyBoardTitleUpdate(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
       // BoardHelper boardHelper = new BoardHelper();

        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO).join();

        assertNotNull(player);

       // boardHelper.add(player);

        Collector<ScoreboardObjectivePacket> collector = connection.trackIncoming(ScoreboardObjectivePacket.class);

      //  boardHelper.updateLobbyTitle(10);

        collector.assertSingle();
        collector.assertSingle(packet -> {
            assertNotNull(packet.objectiveValue());
            String text = PlainTextComponentSerializer.plainText().serialize(packet.objectiveValue());
            assertNotNull(text);
            assertFalse(text.isEmpty());
            assertTrue(text.contains("10"));
            assertTrue(text.contains("Lobby"));
        });

        env.destroyInstance(instance, true);
    }

    @Disabled
    @Test
    void testGameBoardTitleUpdate(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
       // BoardHelper boardHelper = new BoardHelper();

        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO).join();

        assertNotNull(player);

        // boardHelper.add(player);
        // boardHelper.switchToGame();

        Collector<ScoreboardObjectivePacket> collector = connection.trackIncoming(ScoreboardObjectivePacket.class);

        // boardHelper.updateTitle(Component.text("Test"));

        collector.assertSingle();
        collector.assertSingle(packet -> {
            assertNotNull(packet.objectiveValue());
            String text = PlainTextComponentSerializer.plainText().serialize(packet.objectiveValue());
            assertNotNull(text);
            assertFalse(text.isEmpty());
            assertEquals("Test", text);
        });

        env.destroyInstance(instance, true);
    }
}