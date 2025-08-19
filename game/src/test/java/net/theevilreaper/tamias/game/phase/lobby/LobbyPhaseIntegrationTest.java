package net.theevilreaper.tamias.game.phase.lobby;

import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Needs a refactor")
@ExtendWith(MicrotusExtension.class)
class LobbyPhaseIntegrationTest extends LobbyPhaseTestBase {

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
