package net.theevilreaper.tamias.game.phase.lobby;

import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.tamias.game.map.GameMapProvider;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.phase.LobbyPhaseData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.nio.file.Paths;

public abstract class LobbyPhaseTestBase {

    protected static final int LOBBY_PHASE_TIME = 60;
    protected static LobbyPhase lobbyPhase;

    @BeforeAll
    static void setup() {
        MapProvider mapProvider = new GameMapProvider(Paths.get(""));
        LobbyPhaseData data = new LobbyPhaseData(value -> {}, 1,2, LOBBY_PHASE_TIME);
        lobbyPhase = new LobbyPhase(mapProvider, data);
    }

    @AfterEach
    void cleanup() {
        lobbyPhase.setPaused(true);
        lobbyPhase.setCurrentTicks(LOBBY_PHASE_TIME);
        lobbyPhase.setForceStarted(false);
    }
}
