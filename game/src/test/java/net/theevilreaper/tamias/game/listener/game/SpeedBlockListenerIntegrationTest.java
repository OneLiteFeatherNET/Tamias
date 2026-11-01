package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.ground.SpeedBlock;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SpeedBlockListenerIntegrationTest extends TamiasPlayerTestBase {

    private static final Pos START = new Pos(5.5, 40, 0.5);
    // Looks along the positive x axis (yaw -90)
    private static final Pos ON_SPEED_BLOCK = new Pos(0.5, 40, 0.5, -90, 0);

    @Test
    void testStepOntoSpeedBlockBoostsThePlayer(Env env) {
        Instance instance = createInstance(env);
        Player player = env.createPlayer(instance, START);

        new SpeedBlockListener(() -> 0L).accept(new PlayerMoveEvent(player, ON_SPEED_BLOCK, true));

        Vec velocity = player.getVelocity();
        assertEquals(SpeedBlock.DEFAULT_BOOST, velocity.x(), 1e-6);
        assertEquals(0, velocity.z(), 1e-6);
        assertEquals(SpeedBlockListener.VERTICAL_BOOST, velocity.y(), 1e-6);

        env.destroyInstance(instance, true);
    }

    @Test
    void testNormalBlockDoesNothing(Env env) {
        Instance instance = createInstance(env);
        Player player = env.createPlayer(instance, START);

        new SpeedBlockListener(() -> 0L).accept(new PlayerMoveEvent(player, new Pos(3.5, 40, 0.5), true));

        assertNoBoost(player);
        env.destroyInstance(instance, true);
    }

    @Test
    void testSameBlockDoesNothing(Env env) {
        Instance instance = createInstance(env);
        Player player = env.createPlayer(instance, new Pos(0.2, 40, 0.2));

        // Only a small step on the speed block itself, the boost is only given when the player steps onto it
        new SpeedBlockListener(() -> 0L).accept(new PlayerMoveEvent(player, ON_SPEED_BLOCK, true));

        assertNoBoost(player);
        env.destroyInstance(instance, true);
    }

    @Test
    void testInAirDoesNothing(Env env) {
        Instance instance = createInstance(env);
        Player player = env.createPlayer(instance, START);

        new SpeedBlockListener(() -> 0L).accept(new PlayerMoveEvent(player, ON_SPEED_BLOCK, false));

        assertNoBoost(player);
        env.destroyInstance(instance, true);
    }

    @Test
    void testSpectatorsGetNoBoost(Env env) {
        Instance instance = createInstance(env);
        Player lightSpectator = env.createPlayer(instance, START);
        lightSpectator.setTag(Tags.SPECTATOR, true);
        Player spectator = env.createPlayer(instance, START);
        spectator.setGameMode(GameMode.SPECTATOR);

        SpeedBlockListener listener = new SpeedBlockListener(() -> 0L);
        listener.accept(new PlayerMoveEvent(lightSpectator, ON_SPEED_BLOCK, true));
        listener.accept(new PlayerMoveEvent(spectator, ON_SPEED_BLOCK, true));

        assertNoBoost(lightSpectator);
        assertNoBoost(spectator);
        env.destroyInstance(instance, true);
    }

    @Test
    void testCooldown(Env env) {
        Instance instance = createInstance(env);
        Player player = env.createPlayer(instance, START);
        AtomicLong time = new AtomicLong(1000);
        SpeedBlockListener listener = new SpeedBlockListener(time::get);

        listener.accept(new PlayerMoveEvent(player, ON_SPEED_BLOCK, true));
        assertFalse(player.getVelocity().isZero());

        player.setVelocity(Vec.ZERO);
        time.addAndGet(SpeedBlockListener.COOLDOWN_MILLIS - 1);
        listener.accept(new PlayerMoveEvent(player, ON_SPEED_BLOCK, true));
        assertNoBoost(player);

        time.addAndGet(1);
        listener.accept(new PlayerMoveEvent(player, ON_SPEED_BLOCK, true));
        assertFalse(player.getVelocity().isZero());

        env.destroyInstance(instance, true);
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        instance.setBlock(0, 39, 0, SpeedBlock.of(Block.SEA_LANTERN));
        instance.setBlock(3, 39, 0, Block.SEA_LANTERN);
        instance.setBlock(5, 39, 0, Block.STONE);
        return instance;
    }

    private static void assertNoBoost(Player player) {
        assertFalse(player.getVelocity().y() > 0, "The player got a boost");
        assertEquals(0, player.getVelocity().x(), 1e-6);
        assertEquals(0, player.getVelocity().z(), 1e-6);
    }
}
