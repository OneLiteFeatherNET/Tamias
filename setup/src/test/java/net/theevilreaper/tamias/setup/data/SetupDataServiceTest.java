package net.theevilreaper.tamias.setup.data;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.setup.state.SetupState;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class SetupDataServiceTest {

    private static SetupDataService setupDataProvider;
    private static Instance instance;
    private static Player player;

    @BeforeAll
    static void setUp(@NotNull Env env) {
        setupDataProvider = new SetupDataService();
        instance = env.createFlatInstance();
        player = env.createPlayer(instance);
    }

    @AfterEach
    void cleanUp() {
        setupDataProvider.clear();
    }

    @AfterAll
    static void tearDown(@NotNull Env env) {
        setupDataProvider = null;
        env.destroyInstance(instance, true);
        instance = null;
        player = null;
    }

    @Test
    void testPlayerAdd() {
        SetupData data = SetupData.builder()
                .state(SetupState.LOBBY)
                .player(player)
                .build();
        setupDataProvider.addSetupData(player, data);
        assertNotNull(setupDataProvider.getSetupData(player));
    }

    @Test
    void testPlayerRemove() {
        assertFalse(setupDataProvider.removeSetupData(player));

        SetupData data = SetupData.builder()
                .state(SetupState.LOBBY)
                .player(player)
                .build();
        setupDataProvider.addSetupData(player, data);
        assertTrue(setupDataProvider.removeSetupData(player));
    }
}