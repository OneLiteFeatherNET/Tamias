package net.theevilreaper.tamias.game.phase.lobby;

import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.phase.LobbyPhaseData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public abstract class LobbyPhaseTestBase {

    protected static final int LOBBY_PHASE_TIME = 60;
    protected static LobbyPhase lobbyPhase;

    @BeforeAll
    static void setup() {
        LobbyPhaseData data = new LobbyPhaseData(value -> {}, 2,1, LOBBY_PHASE_TIME);
        lobbyPhase = new LobbyPhase(data);
    }

    @AfterEach
    void cleanup() {
        lobbyPhase.setPaused(true);
        lobbyPhase.setCurrentTicks(LOBBY_PHASE_TIME);
        lobbyPhase.setForceStarted(false);
    }
}
