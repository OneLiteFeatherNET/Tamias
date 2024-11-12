package net.theevilreaper.tamias.game.stamina;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
}
