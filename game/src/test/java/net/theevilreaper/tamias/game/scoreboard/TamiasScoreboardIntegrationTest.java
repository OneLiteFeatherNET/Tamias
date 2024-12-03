package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class TamiasScoreboardIntegrationTest {

    private static TamiasScoreboard tamiasScoreboard;

    @BeforeEach
    void setup() {
        tamiasScoreboard = new TamiasBoard();
    }

    @AfterEach
    void teardown() {
        tamiasScoreboard = null;
    }

    @Test
    void testTimeUpdate(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();

        Player player = connection.connect(instance, Pos.ZERO).join();

        tamiasScoreboard.initDefaults();

        Collector<DisplayScoreboardPacket> displayPacketCollector = connection.trackIncoming(DisplayScoreboardPacket.class);

        tamiasScoreboard.addViewer(player);

        displayPacketCollector.assertSingle();
        displayPacketCollector.assertSingle(packet -> assertEquals(1, packet.position()));

        Collector<ScoreboardObjectivePacket> objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);

        tamiasScoreboard.updateTime(12);

        objectiveCollector.assertSingle();
        objectiveCollector.assertSingle(packet -> {
            assertEquals(2, packet.mode());
            assertNull(packet.numberFormat());
            assertNotNull(packet.objectiveValue());
            String content = PlainTextComponentSerializer.plainText().serialize(packet.objectiveValue());
            assertNotNull(content);
            assertFalse(content.isEmpty());
            assertTrue(content.contains("Time: 00:12"));
        });

        env.destroyInstance(instance, true);
    }

    @Test
    void testViewerUpdateWhichIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();

        Player player = connection.connect(instance, Pos.ZERO).join();

        assertTrue(tamiasScoreboard.addViewer(player));

        Collector<DisplayScoreboardPacket> displayPacketCollector = connection.trackIncoming(DisplayScoreboardPacket.class);
        assertFalse(tamiasScoreboard.addViewer(player));
        displayPacketCollector.assertEmpty();

        env.destroyInstance(instance, true);
    }

    @Test
    void testViewerUpRemoveWhichIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();

        Player player = connection.connect(instance, Pos.ZERO).join();

        tamiasScoreboard.addViewer(player);
        assertTrue(tamiasScoreboard.removeViewer(player));

        Collector<ScoreboardObjectivePacket> objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        assertFalse(tamiasScoreboard.removeViewer(player));
        objectiveCollector.assertEmpty();

        env.destroyInstance(instance, true);
    }

    @Disabled(value = "Check why the update scores method doesn't work")
    @Test
    void testUpdateGameDefaults(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();

        Player player = connection.connect(instance, Pos.ZERO).join();

        tamiasScoreboard.switchBoard(BoardType.GAME);
        tamiasScoreboard.addViewer(player);

        Collector<TeamsPacket> lineUpdate = connection.trackIncoming(TeamsPacket.class);

        tamiasScoreboard.updateGameDefaults(10, 10, 1);

        lineUpdate.assertSingle();

        env.destroyInstance(instance, true);
    }

    @Test
    void testResetBoard(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();

        Player player = connection.connect(instance, Pos.ZERO).join();

        tamiasScoreboard.addViewer(player);

        Collector<ScoreboardObjectivePacket> destructionPacket = connection.trackIncoming(ScoreboardObjectivePacket.class);

        tamiasScoreboard.resetBoard();

        destructionPacket.assertSingle();

        env.destroyInstance(instance, true);
    }

    @Test
    void testEmptyViewerGet() {
        assertTrue(tamiasScoreboard.getViewers().isEmpty());
    }
}
