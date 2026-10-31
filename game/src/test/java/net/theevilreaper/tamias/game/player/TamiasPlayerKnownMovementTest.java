package net.theevilreaper.tamias.game.player;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TamiasPlayerKnownMovementTest extends TamiasPlayerTestBase {

    private static final double EPSILON = 1e-6;

    @Test
    void testKnownMovementIsTheDisplacementOfTheLastTick(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TamiasPlayer player = (TamiasPlayer) env.createPlayer(instance, new Pos(0, 60, 0));
        env.tick();

        player.teleport(new Pos(1, 60, 0)).join();
        env.tick();

        Vec knownMovement = player.getKnownMovement();
        assertEquals(1.0, knownMovement.x(), EPSILON, "The x movement of the tick should be known, was " + knownMovement);
        assertEquals(0.0, knownMovement.y(), EPSILON, "No y movement happened, was " + knownMovement);
        assertEquals(0.0, knownMovement.z(), EPSILON, "No z movement happened, was " + knownMovement);
    }

    @Test
    void testKnownMovementIsZeroWhileStanding(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 60, 0));
        env.tick();

        env.tick();

        assertEquals(Vec.ZERO, ((TamiasPlayer) player).getKnownMovement(), "A standing player has no known movement");
    }
}
