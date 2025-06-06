package net.theevilreaper.tamias.setup.data;

import net.theevilreaper.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class SetupDataServiceTest {

    private static SetupDataService setupDataProvider;
    private static Instance instance;
    private static Player player;
    private static MapEntry testEntry;

    @BeforeAll
    static void setUp(@NotNull Env env) {
        setupDataProvider = new SetupDataService();
        instance = env.createFlatInstance();
        player = env.createPlayer(instance);
        testEntry = MapEntry.of(Paths.get(""));
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
        testEntry = null;
    }

    @Test
    void testPlayerAdd() {
        SetupData data = SetupData.builder()
                .player(player)
                .mapEntry(testEntry)
                .build();
        setupDataProvider.addSetupData(player, data);
        assertFalse(data.hasAreaMode());
        assertNotNull(setupDataProvider.getSetupData(player));
    }

    @Test
    void testPlayerRemove() {
        Optional<SetupData> setupData = setupDataProvider.removeSetupData(player);
        assertTrue(setupData.isEmpty());

        SetupData data = SetupData.builder()
                .player(player)
                .mapEntry(testEntry)
                .build();
        setupDataProvider.addSetupData(player, data);
        setupData = setupDataProvider.removeSetupData(player);
        assertTrue(setupData.isPresent());
    }
}