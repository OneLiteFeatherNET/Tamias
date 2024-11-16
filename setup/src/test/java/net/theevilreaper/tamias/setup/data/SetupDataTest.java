package net.theevilreaper.tamias.setup.data;

import de.icevizion.aves.map.BaseMap;
import de.icevizion.aves.map.MapEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.setup.state.SetupState;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class SetupDataTest {

    @ParameterizedTest
    @EnumSource(SetupState.class)
    void testSetupDataCreation(@NotNull SetupState mode, @NotNull Env env) {
        Path testPath = Path.of("");
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        assertNotNull(instance);
        assertNotNull(testPlayer);

        SetupData.Builder builder = SetupData.builder();

        assertNotNull(builder);

        BaseMap baseMap = switch (mode) {
            case LOBBY -> new BaseMap();
            case GAME -> new GameMap();
        };

        SetupData data = builder
                .mapEntry(MapEntry.of(testPath))
                .player(testPlayer)
                .state(mode)
                .baseMap(baseMap)
                .build();

        Class<?> setupDataClass = switch (mode) {
            case LOBBY -> LobbyData.class;
            case GAME -> GameData.class;
        };

        assertInstanceOf(setupDataClass, data);
        assertNotNull(data);
        assertEquals(mode, data.getSetupState());
        assertEquals(testPlayer, data.getPlayer());
        assertEquals(testPath, data.getMapEntry().getDirectoryRoot());

        env.destroyInstance(instance, true);
    }

    @Test
    void testAreaModeSwap(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player testPlayer = env.createPlayer(instance);

        SetupData data = SetupData.builder()
                .player(testPlayer)
                .state(SetupState.GAME)
                .baseMap(new GameMap())
                .build();

        assertNotNull(data);
        assertFalse(data.hasAreaMode());
        data.swapAreaMode();
        assertTrue(data.hasAreaMode());

        env.destroyInstance(instance, true);
    }
}