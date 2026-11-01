package net.theevilreaper.tamias.game.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TamiasPlayerPositionHistoryTest extends TamiasPlayerTestBase {

    private static final long TICK_MILLIS = 50;

    @Test
    void testZeroMillisIsTheCurrentPosition(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, new Pos(0, 40, 0));
        env.tick();

        assertEquals(player.getPosition(), player.getPositionMillisAgo(0));

        env.destroyInstance(instance, true);
    }

    @Test
    void testReturnsThePositionOfEarlierTicks(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, new Pos(0, 40, 0));
        for (int x = 1; x <= 3; x++) {
            player.teleport(new Pos(x, 40, 0)).join();
            env.tick();
        }

        assertEquals(3, player.getPositionMillisAgo(0).x());
        assertEquals(2, player.getPositionMillisAgo(TICK_MILLIS).x());
        assertEquals(1, player.getPositionMillisAgo(2 * TICK_MILLIS).x());

        env.destroyInstance(instance, true);
    }

    @Test
    void testOlderRequestsReturnTheOldestKnownPosition(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, new Pos(0, 40, 0));
        for (int x = 1; x <= 20; x++) {
            player.teleport(new Pos(x, 40, 0)).join();
            env.tick();
        }

        // The history only reaches back as far as the rewind cap
        long maxTicks = TamiasPlayer.POSITION_HISTORY_MILLIS / TICK_MILLIS;
        assertEquals(20 - maxTicks, player.getPositionMillisAgo(10_000).x());

        env.destroyInstance(instance, true);
    }

    @Test
    void testWithoutTicksTheCurrentPositionIsReturned(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, new Pos(0, 40, 0));

        assertEquals(player.getPosition(), player.getPositionMillisAgo(TICK_MILLIS));

        env.destroyInstance(instance, true);
    }
}
