package net.theevilreaper.tamias.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameConfigTest {

    @Test
    void testInvalidLobbyTimeUsage() {
        GameConfig.Builder builder = GameConfig.builder();
        assertNotNull(builder);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.lobbyTime(0));
        assertNotNull(exception);
        assertEquals("Lobby time must be greater than " + GameConfig.FORCE_START_TIME, exception.getMessage());
    }

    @Test
    void testBuilderUsage() {
        GameConfig.Builder builder = GameConfig.builder();
        assertNotNull(builder);

        builder.gameTime(500).lobbyTime(12).minPlayers(1).maxPlayers(12).teamSize(12);

        GameConfig config = builder.build();

        assertEquals(500, config.gameTime());
        assertEquals(12, config.lobbyTime());
        assertEquals(1, config.minPlayers());
        assertEquals(12, config.maxPlayers());
        assertEquals(12, config.teamSize());
    }

}