package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class LobbyScoreboardIntegrationTest {

    private static LobbyScoreboard lobbyScoreboard;

    @BeforeAll
    static void setup() {
        lobbyScoreboard = new LobbyScoreboard(Component.text("Lobby Scoreboard"));
        lobbyScoreboard.initDefaults();
    }

    @Test
    void testScoreboardDefaults(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);

        Collector<TeamsPacket> collector = connection.trackIncoming(TeamsPacket.class);
        lobbyScoreboard.addViewer(player);
        env.tickWhile(() -> true, Duration.ofSeconds(1));
        // Assert that the scoreboard has been initialized with default values
        collector.assertCount(7);

        lobbyScoreboard.removeViewer(player);
        env.destroyInstance(instance, true);
        assertTrue(instance.getPlayers().isEmpty(), "Instance should have no players after destruction");
    }

    @Test
    void testDoublePlayerAddToSideBar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);

        lobbyScoreboard.addViewer(player);
        env.tickWhile(() -> true, Duration.ofSeconds(1));
        Collector<TeamsPacket> collector = connection.trackIncoming(TeamsPacket.class);
        lobbyScoreboard.addViewer(player);

        // Assert that adding the same player again does not send another packet
        collector.assertCount(6);
        env.destroyInstance(instance, true);
        assertTrue(instance.getPlayers().isEmpty(), "Instance should have no players after destruction");
    }

    @Test
    void testDoublePlayerRemoveFromSideBar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);

        lobbyScoreboard.addViewer(player);
        lobbyScoreboard.removeViewer(player);

        Collector<TeamsPacket> collector = connection.trackIncoming(TeamsPacket.class);
        lobbyScoreboard.removeViewer(player);

        collector.assertEmpty();
        env.destroyInstance(instance, true);
        assertTrue(instance.getPlayers().isEmpty(), "Instance should have no players after destruction");
    }

    @Test
    void testInvalidScoreUpdateType() {
        assertAll(
                "Invalid score type for LobbyScoreboard",
                () -> assertThrows(UnsupportedOperationException.class, () ->
                        lobbyScoreboard.updateScore(ScoreType.TNT, Component.text("TNT Count"))
                ),
                () -> assertThrows(UnsupportedOperationException.class, () ->
                        lobbyScoreboard.updateScore(ScoreType.ROUND_TYPE, Component.text("Round Type"))
                )
        );
    }
}
