package net.theevilreaper.tamias.game.phase;

import de.icevizion.aves.map.MapEntry;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.map.MapProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class LobbyPhaseIntegrationTest {

    private static final int LOBBY_PHASE_TIME = 60;
    private static LobbyPhase lobbyPhase;

    @BeforeAll
    static void setup() {
        MapProvider mapProvider = new MapProvider(Paths.get(""), pathStream -> List.of(MapEntry.of(Path.of(""))));
        lobbyPhase = new LobbyPhase(mapProvider, 1, 2, LOBBY_PHASE_TIME, value -> {});
    }

    @AfterEach
    void cleanup() {
        lobbyPhase.setPaused(true);
        lobbyPhase.setCurrentTicks(LOBBY_PHASE_TIME);
        lobbyPhase.setForceStarted(false);
    }

    @Test
    void testForceStart() {
        assertFalse(lobbyPhase.isForceStarted());
        assertEquals(LOBBY_PHASE_TIME, lobbyPhase.getCurrentTicks());
        lobbyPhase.setForceStarted(true);
        assertTrue(lobbyPhase.isForceStarted());
        assertNotEquals(LOBBY_PHASE_TIME, lobbyPhase.getCurrentTicks());
        assertEquals(GameConfig.FORCE_START_TIME, lobbyPhase.getCurrentTicks());
    }
}
