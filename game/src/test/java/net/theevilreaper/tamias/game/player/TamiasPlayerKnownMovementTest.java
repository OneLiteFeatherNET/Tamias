package net.theevilreaper.tamias.game.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket;
import net.minestom.testing.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TamiasPlayerKnownMovementTest extends TamiasPlayerTestBase {

    private static final double EPSILON = 1e-6;
    private static final Pos SPAWN = new Pos(0.5, 40, 0.5);

    @Test
    void testKnownMovementIsTheMovementOfTheLastPacket(@NotNull Env env) {
        TamiasPlayer player = spawnMovablePlayer(env);

        // Two packets arrive in the same tick, the movement of one packet is 0.3 blocks
        move(player, 0.3);
        move(player, 0.6);
        env.tick();

        assertMovementX(0.3, player.getKnownMovement());
    }

    @Test
    void testKnownMovementIsKeptWhenATickHasNoPacket(@NotNull Env env) {
        TamiasPlayer player = spawnMovablePlayer(env);

        move(player, 0.3);
        env.tick();
        // The next packet is late, a shot in the following tick must still know the movement
        env.tick();

        assertMovementX(0.3, player.getKnownMovement());
    }

    @Test
    void testKnownMovementExpiresWithoutPackets(@NotNull Env env) {
        TamiasPlayer player = spawnMovablePlayer(env);

        move(player, 0.3);
        env.tick();
        env.tick();
        env.tick();

        assertEquals(Vec.ZERO, player.getKnownMovement(), "A player without movement packets has no known movement");
    }

    @Test
    void testKnownMovementIsKeptWhenOnlyTheViewChanges(@NotNull Env env) {
        TamiasPlayer player = spawnMovablePlayer(env);

        move(player, 0.3);
        // Using an item updates the view through the same movement path, it must not erase the movement
        player.addPacketToQueue(new ClientPlayerRotationPacket(90f, 10f, true, false));
        env.tick();

        assertMovementX(0.3, player.getKnownMovement());
    }

    @Test
    void testTeleportResetsKnownMovement(@NotNull Env env) {
        TamiasPlayer player = spawnMovablePlayer(env);
        move(player, 0.3);
        env.tick();

        player.teleport(SPAWN.add(10, 0, 0)).join();
        env.tick();

        assertEquals(Vec.ZERO, player.getKnownMovement(), "A teleport is no movement of the player");
    }

    @Test
    void testKnownMovementIsZeroWhileStanding(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, SPAWN);
        env.tick();

        env.tick();

        assertEquals(Vec.ZERO, ((TamiasPlayer) player).getKnownMovement(), "A standing player has no known movement");
    }

    private static TamiasPlayer spawnMovablePlayer(Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, SPAWN);
        // Minestom ignores movement packets until the client confirmed the spawn teleport
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));
        env.tick();
        return player;
    }

    private static void move(TamiasPlayer player, double x) {
        player.addPacketToQueue(new ClientPlayerPositionPacket(SPAWN.add(x, 0, 0), true, false));
    }

    private static void assertMovementX(double expected, Vec knownMovement) {
        assertEquals(expected, knownMovement.x(), EPSILON, "The x movement should be known, was " + knownMovement);
        assertEquals(0.0, knownMovement.y(), EPSILON, "No y movement happened, was " + knownMovement);
        assertEquals(0.0, knownMovement.z(), EPSILON, "No z movement happened, was " + knownMovement);
    }
}
