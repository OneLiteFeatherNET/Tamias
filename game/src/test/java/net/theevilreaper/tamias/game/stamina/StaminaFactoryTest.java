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
class StaminaFactoryTest {

    @Test
    void testCreateExplodeBar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);
        assertNotNull(staminaBar);
        assertInstanceOf(ExplodeBar.class, staminaBar);
        assertEquals(player.getUuid(), staminaBar.player.getUuid());

        env.destroyInstance(instance, true);
    }

    @Test
    void testCreateShootBar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        StaminaBar staminaBar = StaminaFactory.createShootBar(player);
        assertNotNull(staminaBar);
        assertInstanceOf(ShootBar.class, staminaBar);
        assertEquals(player.getUuid(), staminaBar.player.getUuid());

        env.destroyInstance(instance, true);
    }

}