package net.theevilreaper.tamias.game.stamina;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class StaminaBarIntegrationTest {

    @Test
    void testStaminaBarEqualsCheck(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        Player firstPlayer = env.createPlayer(instance);
        Player secondPlayer = env.createPlayer(instance);

        StaminaBar firstExplodeBar = StaminaFactory.createExplodeBar(firstPlayer);
        StaminaBar firstShootBar = StaminaFactory.createShootBar(firstPlayer);

        assertNotEquals(firstExplodeBar, firstShootBar);
        assertEquals(firstExplodeBar, StaminaFactory.createExplodeBar(firstPlayer));

        assertNotEquals(firstExplodeBar, StaminaFactory.createExplodeBar(secondPlayer));

        assertEquals(firstExplodeBar.hashCode(), StaminaFactory.createExplodeBar(firstPlayer).hashCode());
        assertNotEquals(firstExplodeBar.hashCode(), StaminaFactory.createExplodeBar(secondPlayer).hashCode());

        env.destroyInstance(instance, true);
    }

    @Test
    void testStartAndStopIdempotency(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        StaminaBar bar = StaminaFactory.createShootBar(player);

        // Calling stop when not started should do nothing and keep status null
        assertDoesNotThrow(bar::stop);
        assertNull(bar.status);

        // Calling start initializes status
        bar.start();
        assertEquals(StaminaBar.Status.READY, bar.status);

        // Calling start again when task is already running should be a no-op
        assertDoesNotThrow(bar::start);
        assertEquals(StaminaBar.Status.READY, bar.status);

        // Stopping active bar resets status to null
        bar.stop();
        assertNull(bar.status);

        // Calling stop again when already stopped should be a no-op
        assertDoesNotThrow(bar::stop);
        assertNull(bar.status);

        env.destroyInstance(instance, true);
    }
}
