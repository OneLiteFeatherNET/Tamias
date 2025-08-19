package net.theevilreaper.tamias.game.util.phase;

import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.config.GameConfigReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

class LobbyPhaseDataTest {

    private final IntConsumer dummyUpdater = new AtomicInteger()::set;

    @Test
    void testDataCreationViaConfiguration() {
        LobbyPhaseData data = new LobbyPhaseData(dummyUpdater, new GameConfigReader(Paths.get("")).getConfig());
        assertNotNull(data);

        assertEquals(13, data.maxPlayers());
        assertEquals(1, data.minPlayers());
        assertEquals(30, data.lobbyTime());
        assertSame(dummyUpdater, data.timeUpdater());
    }

    @Test
    void validLobbyPhaseDataShouldCreateSuccessfully() {
        LobbyPhaseData data = new LobbyPhaseData(dummyUpdater, 10, 2, 60);

        assertEquals(10, data.maxPlayers());
        assertEquals(2, data.minPlayers());
        assertEquals(60, data.lobbyTime());
        assertSame(dummyUpdater, data.timeUpdater());
    }

    @Test
    void minPlayersZeroShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new LobbyPhaseData(dummyUpdater, 10, 0, 60)
        );
        assertEquals("minPlayers must be greater than 0", exception.getMessage());
    }

    @Test
    void maxPlayersLessThanMinPlayersShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new LobbyPhaseData(dummyUpdater, 3, 5, 60)
        );
        assertEquals("maxPlayers must be greater than or equal to minPlayers", exception.getMessage());
    }

    @Test
    void lobbyTimeZeroShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new LobbyPhaseData(dummyUpdater, 10, 2, 0)
        );
        assertEquals("lobbyTime must be greater than 0", exception.getMessage());
    }

    @Test
    void lobbyTimeNegativeShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new LobbyPhaseData(dummyUpdater, 10, 2, -30)
        );
        assertEquals("lobbyTime must be greater than 0", exception.getMessage());
    }

    @Test
    void maxEqualsMinPlayersShouldBeValid() {
        LobbyPhaseData data = new LobbyPhaseData(dummyUpdater, 5, 5, 30);

        assertEquals(5, data.maxPlayers());
        assertEquals(5, data.minPlayers());
        assertEquals(30, data.lobbyTime());
    }
}
