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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class BoardHelperIntegrationTest {

    private static TamiasScoreboard tamiasScoreboard;

    @BeforeAll
    static void setup() {
        tamiasScoreboard = TamiasScoreboard.create();
        tamiasScoreboard.initDefaults();
    }

    @AfterEach
    void reset() {
        tamiasScoreboard.resetBoard();
    }

    @AfterAll
    static void cleanup() {
        tamiasScoreboard = null;
    }

    @Test
    void testLobbyBoardTitleUpdate(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO);

        assertNotNull(player);
        tamiasScoreboard.switchBoard(TamiasScoreboard.BoardType.LOBBY);
        tamiasScoreboard.addViewer(player);

        Collector<ScoreboardObjectivePacket> collector = connection.trackIncoming(ScoreboardObjectivePacket.class);

        tamiasScoreboard.updateTime(10);

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

    @Test
    void testGameBoardTitleUpdate(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance, Pos.ZERO);

        assertNotNull(player);

        tamiasScoreboard.addViewer(player);
        tamiasScoreboard.switchBoard(TamiasScoreboard.BoardType.GAME);

        Collector<ScoreboardObjectivePacket> collector = connection.trackIncoming(ScoreboardObjectivePacket.class);

        tamiasScoreboard.updateTime(10);

        collector.assertSingle();
        collector.assertSingle(packet -> {
            assertNotNull(packet.objectiveValue());
            String text = PlainTextComponentSerializer.plainText().serialize(packet.objectiveValue());
            assertNotNull(text);
            assertFalse(text.isEmpty());
            assertTrue(text.contains("10"));
        });

        env.destroyInstance(instance, true);
    }
}