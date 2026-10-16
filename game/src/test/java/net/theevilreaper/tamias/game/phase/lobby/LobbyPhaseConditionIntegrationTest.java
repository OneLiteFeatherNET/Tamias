package net.theevilreaper.tamias.game.phase.lobby;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.phase.LobbyPhaseData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class LobbyPhaseConditionIntegrationTest extends LobbyPhaseTestBase {

    @Test
    void testLobbyPhasePlayerUpdate(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        Player player = env.createPlayer(instance);

        lobbyPhase.updatePlayerValues(player);

        assertEquals(1, player.getExp());
        assertEquals(LOBBY_PHASE_TIME, player.getLevel());
    }

    @Test
    void testCanStart(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        lobbyPhase.checkStartCondition();
        assertTrue(lobbyPhase.isPaused());

        env.createPlayer(instance);
        lobbyPhase.checkStartCondition();
        assertFalse(lobbyPhase.isPaused());

        env.createPlayer(instance);
        lobbyPhase.checkStartCondition();
        assertFalse(lobbyPhase.isPaused());

        env.destroyInstance(instance, true);
    }

    @Test
    void testPhaseStopCondition(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        LobbyPhase phase = new LobbyPhase(new LobbyPhaseData(_ -> {}, 3, 2, LOBBY_PHASE_TIME));
        assertTrue(phase.isPaused());

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            players.add(env.createPlayer(instance));
        }

        phase.checkStartCondition();
        assertFalse(phase.isPaused());
        phase.setCurrentTicks(LOBBY_PHASE_TIME - 10);

        int randomIndex = ThreadLocalRandom.current().nextInt(0, players.size());
        Player player = players.remove(randomIndex);
        phase.checkStopCondition(player);
        assertTrue(phase.isPaused());

        Player lastPlayer = players.getFirst();
        assertEquals(LOBBY_PHASE_TIME, phase.getCurrentTicks());
        assertEquals(LOBBY_PHASE_TIME, lastPlayer.getLevel());

        env.destroyInstance(instance, true);
    }

    @Test
    void testPhaseKeepsRunningWithEnoughPlayers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        LobbyPhase phase = new LobbyPhase(new LobbyPhaseData(value -> {}, 3, 2, LOBBY_PHASE_TIME));
        Player leavingPlayer = env.createPlayer(instance);
        env.createPlayer(instance);
        env.createPlayer(instance);

        phase.checkStartCondition();
        phase.setCurrentTicks(LOBBY_PHASE_TIME - 10);
        phase.checkStopCondition(leavingPlayer);

        assertFalse(phase.isPaused());
        assertEquals(LOBBY_PHASE_TIME - 10, phase.getCurrentTicks());

        env.destroyInstance(instance, true);
    }
}
