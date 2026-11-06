package net.theevilreaper.tamias.game.commands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.onelitefeather.tamias.stats.player.PlayerStatsView;
import net.onelitefeather.tamias.stats.player.TamiasStatsPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.stats.FakeStatsRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsCommandIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testShowsOwnStats(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        FakeStatsRepository repository = new FakeStatsRepository(true);
        repository.addTotals(new PlayerStatsView(new TamiasStatsPlayer(player.getUuid(), player.getUsername()),
                5, 3, 2, 1, 4, 7));
        env.process().command().register(new StatsCommand(repository));
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        env.process().command().execute(player, "stats");

        String text = plainText(messages);
        assertTrue(text.contains("Statistics of " + player.getUsername()), text);
        assertTrue(text.contains("Rounds: 5 · Wins: 3 (2 as survivor, 1 as bomber)"), text);
        assertTrue(text.contains("Kills: 4 · Hits: 7"), text);

        env.destroyInstance(instance, true);
    }

    @Test
    void testShowsStatsOfOfflinePlayerByName(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        FakeStatsRepository repository = new FakeStatsRepository(true);
        repository.addTotals(new PlayerStatsView(new TamiasStatsPlayer(UUID.randomUUID(), "Alice"), 2, 1, 1, 0, 0, 3));
        env.process().command().register(new StatsCommand(repository));
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        env.process().command().execute(player, "stats alice");

        String text = plainText(messages);
        assertTrue(text.contains("Statistics of Alice"), text);
        assertTrue(text.contains("Rounds: 2"), text);

        env.destroyInstance(instance, true);
    }

    @Test
    void testUnknownPlayerHasNoStats(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        env.process().command().register(new StatsCommand(new FakeStatsRepository(true)));
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        env.process().command().execute(player, "stats Nobody");

        String text = plainText(messages);
        assertTrue(text.contains("No statistics found for Nobody"), text);

        env.destroyInstance(instance, true);
    }

    @Test
    void testDisabledStatsAreReported(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        env.process().command().register(new StatsCommand(new FakeStatsRepository(false)));
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        env.process().command().execute(player, "stats");

        String text = plainText(messages);
        assertTrue(text.contains("Statistics are not available"), text);

        env.destroyInstance(instance, true);
    }

    @Test
    void testFailedLookupIsReported(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player player = connection.connect(instance);
        FakeStatsRepository repository = new FakeStatsRepository(true);
        repository.setFailing(true);
        env.process().command().register(new StatsCommand(repository));
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        env.process().command().execute(player, "stats");

        String text = plainText(messages);
        assertTrue(text.contains("could not be loaded"), text);

        env.destroyInstance(instance, true);
    }

    private static String plainText(Collector<SystemChatPacket> collector) {
        StringBuilder builder = new StringBuilder();
        for (SystemChatPacket packet : collector.collect()) {
            builder.append(PlainTextComponentSerializer.plainText().serialize(packet.message())).append('\n');
        }
        return builder.toString();
    }
}
