package net.theevilreaper.tamias.setup.data;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.onelitefeather.guira.data.SetupData;
import net.theevilreaper.aves.map.MapEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MicrotusExtension.class)
class SetupDataFactoryIntegrationTest {

    private static Path path;

    @BeforeAll
    static void init() {
        path = Paths.get("");
    }

    @ParameterizedTest
    @CsvSource({
            "true, LobbyData",
            "false, GameData"
    })
    void testDataCreation(boolean lobby, String expectedType, Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        SetupData setupData = SetupDataFactory.create(player, MapEntry.of(path), lobby);

        assertInstanceOf(InstanceSetupData.class, setupData);

        if (expectedType.equals("LobbyData")) {
            assertInstanceOf(LobbyData.class, setupData);
        } else {
            assertInstanceOf(GameData.class, setupData);
        }

        assertEquals(player.getUuid(), setupData.getId());

        env.destroyInstance(instance, true);
    }
}
