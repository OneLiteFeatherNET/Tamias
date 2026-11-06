package net.theevilreaper.tamias.game.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientPlayerPositionAndRotationPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.network.packet.client.play.ClientTeleportConfirmPacket;
import net.minestom.testing.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The known movement is the movement of the last position packet, like vanilla's known client movement.
 */
class TamiasPlayerKnownMovementTest extends TamiasPlayerTestBase {

    private static final double EPSILON = 1e-6;
    private static final byte ON_GROUND = 0x01;
    private static final Pos START = new Pos(0, 40, 0);

    @Test
    void testKnownMovementIsTheMovementOfTheLastPacket(@NotNull Env env) {
        TamiasPlayer player = spawn(env);

        move(player, START.add(0, 0, 0.28));
        env.tick();

        assertMovement(player, 0, 0, 0.28);
    }

    @Test
    void testTwoPacketsInOneTickCountOnlyTheLastPacket(@NotNull Env env) {
        TamiasPlayer player = spawn(env);

        // A late packet arrives together with the next one, the client still moved 0.28 per client tick
        move(player, START.add(0, 0, 0.28));
        move(player, START.add(0, 0, 0.56));
        env.tick();

        assertMovement(player, 0, 0, 0.28);
    }

    @Test
    void testATickWithoutAPacketKeepsTheMovement(@NotNull Env env) {
        TamiasPlayer player = spawn(env);
        move(player, START.add(0, 0, 0.28));
        env.tick();

        // The next packet is late, the player is still running
        env.tick();

        assertMovement(player, 0, 0, 0.28);
    }

    @Test
    void testMovementIsForgottenWhenNoPacketArrivesAnymore(@NotNull Env env) {
        TamiasPlayer player = spawn(env);
        move(player, START.add(0, 0, 0.28));
        env.tick();

        // A client which stopped sends no position packets
        env.tick();
        env.tick();

        assertEquals(Vec.ZERO, player.getKnownMovement());
    }

    @Test
    void testARotationPacketKeepsTheMovement(@NotNull Env env) {
        TamiasPlayer player = spawn(env);
        move(player, START.add(0, 0, 0.28));
        player.addPacketToQueue(new ClientPlayerRotationPacket(90, 10, ON_GROUND));
        env.tick();

        assertMovement(player, 0, 0, 0.28);
    }

    @Test
    void testATeleportIsNoMovement(@NotNull Env env) {
        TamiasPlayer player = spawn(env);
        move(player, START.add(0, 0, 0.28));
        env.tick();

        player.teleport(new Pos(20, 40, 20)).join();

        assertEquals(Vec.ZERO, player.getKnownMovement());
    }

    @Test
    void testKnownMovementIsZeroWhileStanding(@NotNull Env env) {
        TamiasPlayer player = spawn(env);

        env.tick();

        assertEquals(Vec.ZERO, player.getKnownMovement(), "A standing player has no known movement");
    }

    private static TamiasPlayer spawn(Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, START);
        // Minestom ignores movement packets until the spawn teleport is confirmed
        player.addPacketToQueue(new ClientTeleportConfirmPacket(player.getLastSentTeleportId()));
        env.tick();
        return player;
    }

    private static void move(TamiasPlayer player, Pos position) {
        player.addPacketToQueue(new ClientPlayerPositionAndRotationPacket(position, ON_GROUND));
    }

    private static void assertMovement(TamiasPlayer player, double x, double y, double z) {
        Vec movement = player.getKnownMovement();
        assertEquals(x, movement.x(), EPSILON, "movement x, was " + movement);
        assertEquals(y, movement.y(), EPSILON, "movement y, was " + movement);
        assertEquals(z, movement.z(), EPSILON, "movement z, was " + movement);
    }
}
