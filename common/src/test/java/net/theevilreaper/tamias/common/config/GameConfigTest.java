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
    void testInvalidMinPlayersUsage() {
        GameConfig.Builder builder = GameConfig.builder();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.minPlayers(1));
        assertEquals("The min players must be at least 2", exception.getMessage());
    }

    @Test
    void testTeamSizeMustFitEveryPlayer() {
        GameConfig.Builder builder = GameConfig.builder().minPlayers(2).maxPlayers(12).teamSize(11);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("The team size must be at least the max players", exception.getMessage());
    }

    @Test
    void testDefaultRoundTakesFiveMinutes() {
        assertEquals(300, InternalGameConfig.defaultConfig().gameTime());
    }

    @Test
    void testDefaultConfigHasNoSentryDsn() {
        assertNull(InternalGameConfig.defaultConfig().sentryDsn(), "error reporting must be opt-in");
    }

    @Test
    void testBuilderKeepsTheSentryDsn() {
        GameConfig config = GameConfig.builder().minPlayers(2).maxPlayers(12).teamSize(12)
                .sentryDsn("https://key@sentry.example/1").build();

        assertEquals("https://key@sentry.example/1", config.sentryDsn());
    }

    @Test
    void testBuilderTurnsABlankSentryDsnIntoNull() {
        GameConfig config = GameConfig.builder().minPlayers(2).maxPlayers(12).teamSize(12)
                .sentryDsn("   ").build();

        assertNull(config.sentryDsn(), "a blank DSN means reporting stays off");
    }

    @Test
    void testBuilderUsage() {
        GameConfig.Builder builder = GameConfig.builder();
        assertNotNull(builder);

        builder.gameTime(500).lobbyTime(12).minPlayers(2).maxPlayers(12).teamSize(12);

        GameConfig config = builder.build();

        assertEquals(500, config.gameTime());
        assertEquals(12, config.lobbyTime());
        assertEquals(2, config.minPlayers());
        assertEquals(12, config.maxPlayers());
        assertEquals(12, config.teamSize());
    }

}