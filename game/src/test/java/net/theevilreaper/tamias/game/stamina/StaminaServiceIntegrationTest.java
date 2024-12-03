package net.theevilreaper.tamias.game.stamina;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class StaminaServiceIntegrationTest {

    private static StaminaService staminaService;

    @BeforeEach
    void setUp() {
        staminaService = new StaminaService();
    }

    @AfterEach
    void tearDown() {
        staminaService.cleanUp();
    }

    @AfterAll
    static void afterAll() {
        staminaService.cleanUp();
        staminaService = null;
    }

    @Test
    void testStaminaAdd(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);

        assertNotNull(staminaBar);
        assertInstanceOf(ExplodeBar.class, staminaBar);

        Map<UUID, StaminaBar> staminaBars = new HashMap<>();
        staminaBars.put(player.getUuid(), staminaBar);

        assertEquals(1, staminaBars.size());

        staminaService.addStaminas(staminaBars);

        assertNotNull(staminaService.getStaminaBar(player.getUuid()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testStaminaRemove(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);

        assertNotNull(staminaBar);
        assertInstanceOf(ExplodeBar.class, staminaBar);

        Map<UUID, StaminaBar> staminaBars = new HashMap<>();
        staminaBars.put(player.getUuid(), staminaBar);

        assertEquals(1, staminaBars.size());

        staminaService.addStaminas(staminaBars);

        assertTrue(staminaService.removeStaminaBar(player.getUuid()));
        assertFalse(staminaService.removeStaminaBar(player.getUuid()));
        env.destroyInstance(instance, true);
    }

    @Test
    void testStaminaGet(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player firstPlayer = env.createPlayer(instance);
        StaminaBar staminaBar = StaminaFactory.createExplodeBar(firstPlayer);
        staminaService.addStaminas(Map.of(firstPlayer.getUuid(), staminaBar));

        Player secondPlayer = env.createPlayer(instance);

        assertNotNull(staminaService.getStaminaBar(firstPlayer));
        assertNull(staminaService.getStaminaBar(secondPlayer));

        env.destroyInstance(instance, true);
    }
}
