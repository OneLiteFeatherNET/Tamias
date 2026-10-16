package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class GameScoreboardIntegrationTest {

    @Test
    void testInvalidScoreTypeUsage() {
        GameScoreboard gameScoreboard = new GameScoreboard();
        assertThrowsExactly(
                UnsupportedOperationException.class,
                () -> gameScoreboard.updateScore(ScoreType.MAP_TYPE, Component.empty()),
                        "Cannot update map score in GameScoreboard"
        );
    }

    @Test
    void testAddViewerCreatesOwnSidebar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player first = env.createPlayer(instance);
        Player second = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();

        gameScoreboard.addViewer(first);
        gameScoreboard.addViewer(second);

        Sidebar firstSidebar = gameScoreboard.getSidebar(first);
        Sidebar secondSidebar = gameScoreboard.getSidebar(second);
        assertNotNull(firstSidebar);
        assertNotNull(secondSidebar);
        assertNotSame(firstSidebar, secondSidebar);
        assertNotNull(firstSidebar.getLine(ScoreType.WINS.key().asString()));
        assertTrue(firstSidebar.getViewers().contains(first));

        env.destroyInstance(instance, true);
    }

    @Test
    void testAddViewerTwiceKeepsSidebar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();

        gameScoreboard.addViewer(player);
        Sidebar sidebar = gameScoreboard.getSidebar(player);
        gameScoreboard.addViewer(player);

        assertSame(sidebar, gameScoreboard.getSidebar(player));

        env.destroyInstance(instance, true);
    }

    @Test
    void testAddViewerTwiceSendsNoPackets(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(player);
        Collector<DisplayScoreboardPacket> displayPackets = connection.trackIncoming(DisplayScoreboardPacket.class);

        gameScoreboard.addViewer(player);

        displayPackets.assertEmpty();

        env.destroyInstance(instance, true);
    }

    @Test
    void testSharedUpdateReachesAllViewers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player first = env.createPlayer(instance);
        Player second = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(first);
        gameScoreboard.addViewer(second);

        gameScoreboard.updateScore(ScoreType.TNT, Component.text("12"));

        assertEquals("» 12", lineText(gameScoreboard.getSidebar(first), ScoreType.TNT));
        assertEquals("» 12", lineText(gameScoreboard.getSidebar(second), ScoreType.TNT));

        env.destroyInstance(instance, true);
    }

    @Test
    void testLateViewerGetsSharedValues(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        Component title = Component.text("Time: 04:59");

        gameScoreboard.updateTitle(title);
        gameScoreboard.updateScore(ScoreType.TNT, Component.text("12"));
        gameScoreboard.addViewer(player);

        Sidebar sidebar = gameScoreboard.getSidebar(player);
        assertEquals("» 12", lineText(sidebar, ScoreType.TNT));
        assertEquals(title, sidebar.getTitle());

        env.destroyInstance(instance, true);
    }

    @Test
    void testPlayerUpdateOnlyReachesThatPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player first = env.createPlayer(instance);
        Player second = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(first);
        gameScoreboard.addViewer(second);
        String defaultWins = lineText(gameScoreboard.getSidebar(second), ScoreType.WINS);

        gameScoreboard.updateScore(first, ScoreType.WINS, Component.text("1 / 3"));

        assertEquals("» 1 / 3", lineText(gameScoreboard.getSidebar(first), ScoreType.WINS));
        assertEquals(defaultWins, lineText(gameScoreboard.getSidebar(second), ScoreType.WINS));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRemoveViewerDropsSidebar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(player);
        Sidebar sidebar = gameScoreboard.getSidebar(player);

        gameScoreboard.removeViewer(player);

        assertNull(gameScoreboard.getSidebar(player));
        assertFalse(sidebar.getViewers().contains(player));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRemoveAllViewersHidesEverySidebar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player first = env.createPlayer(instance);
        Player second = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(first);
        gameScoreboard.addViewer(second);
        Sidebar firstSidebar = gameScoreboard.getSidebar(first);

        gameScoreboard.removeAllViewers();

        assertNull(gameScoreboard.getSidebar(first));
        assertNull(gameScoreboard.getSidebar(second));
        assertFalse(firstSidebar.getViewers().contains(first));

        env.destroyInstance(instance, true);
    }

    @Test
    void testSharedValuesSurviveHiding(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(player);
        gameScoreboard.removeAllViewers();

        gameScoreboard.updateScore(ScoreType.ROUND_TYPE, Component.text("2 / 3"));
        gameScoreboard.addViewer(player);

        assertEquals("» 2 / 3", lineText(gameScoreboard.getSidebar(player), ScoreType.ROUND_TYPE));

        env.destroyInstance(instance, true);
    }

    private static String lineText(Sidebar sidebar, ScoreType scoreType) {
        Sidebar.ScoreboardLine line = sidebar.getLine(scoreType.key().asString());
        assertNotNull(line);
        return PlainTextComponentSerializer.plainText().serialize(line.getContent());
    }
}
