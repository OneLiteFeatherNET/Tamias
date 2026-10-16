package net.theevilreaper.tamias.game.round;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameResultAnnouncerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testEachPlayerGetsOnlyOwnResult(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection firstConnection = env.createConnection();
        TestConnection secondConnection = env.createConnection();
        TamiasPlayer first = (TamiasPlayer) firstConnection.connect(instance);
        TamiasPlayer second = (TamiasPlayer) secondConnection.connect(instance);
        first.addRoundWin();
        first.addRoundWin();
        for (int i = 0; i < 4; i++) first.addKill();
        for (int i = 0; i < 7; i++) first.addHit();
        second.addRoundWin();
        Collector<SystemChatPacket> firstMessages = firstConnection.trackIncoming(SystemChatPacket.class);
        Collector<SystemChatPacket> secondMessages = secondConnection.trackIncoming(SystemChatPacket.class);

        GameResultAnnouncer.announce(3, List.of(first, second));

        firstMessages.assertSingle();
        secondMessages.assertSingle();
        String firstText = plainText(firstMessages);
        String secondText = plainText(secondMessages);
        assertTrue(firstText.contains("You won 2 / 3 rounds · 4 kills · 7 hits"), firstText);
        assertTrue(secondText.contains("You won 1 / 3 rounds · 0 kills · 0 hits"), secondText);
        assertFalse(firstText.contains(second.getUsername()), firstText);
        assertFalse(secondText.contains(first.getUsername()), secondText);

        env.destroyInstance(instance, true);
    }

    @Test
    void testNoMessagesWithoutPlayedRounds(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        TamiasPlayer player = (TamiasPlayer) connection.connect(instance);
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        GameResultAnnouncer.announce(0, List.of(player));

        messages.assertEmpty();

        env.destroyInstance(instance, true);
    }

    private static String plainText(Collector<SystemChatPacket> messages) {
        List<SystemChatPacket> packets = messages.collect();
        assertEquals(1, packets.size());
        return PlainTextComponentSerializer.plainText().serialize(packets.getFirst().message());
    }
}
