package net.onelitefeather.tamias.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsConfigTest {

    private static Properties properties(String... pairs) {
        Properties properties = new Properties();
        for (int i = 0; i < pairs.length; i += 2) {
            properties.setProperty(pairs[i], pairs[i + 1]);
        }
        return properties;
    }

    @Test
    void testWithoutUrlIsDisabled() {
        StatsConfig config = StatsConfig.read(properties("stats.username", "tamias"), _ -> null);
        assertSame(StatsConfig.DISABLED, config);
        assertFalse(config.enabled());
    }

    @Test
    void testBlankUrlIsDisabled() {
        assertFalse(StatsConfig.read(properties("stats.jdbc-url", "   "), _ -> null).enabled());
    }

    @Test
    void testReadsAllValues() {
        StatsConfig config = StatsConfig.read(properties(
                "stats.jdbc-url", "jdbc:postgresql://db/game",
                "stats.username", "tamias",
                "stats.password", "secret",
                "stats.pool-size", "8",
                "stats.schema", "round_stats"), _ -> null);
        assertTrue(config.enabled());
        assertEquals("jdbc:postgresql://db/game", config.jdbcUrl());
        assertEquals("tamias", config.username());
        assertEquals("secret", config.password());
        assertEquals(8, config.poolSize());
        assertEquals("round_stats", config.schema());
    }

    @Test
    void testDefaults() {
        StatsConfig config = StatsConfig.read(properties("stats.jdbc-url", "jdbc:postgresql://db/game"), _ -> null);
        assertEquals("", config.username());
        assertEquals("", config.password());
        assertEquals(StatsConfig.DEFAULT_POOL_SIZE, config.poolSize());
        assertEquals(StatsConfig.DEFAULT_SCHEMA, config.schema());
    }

    @Test
    void testPasswordFromEnvironment() {
        StatsConfig config = StatsConfig.read(properties("stats.jdbc-url", "jdbc:postgresql://db/game"),
                name -> StatsConfig.PASSWORD_ENV.equals(name) ? "from-env" : null);
        assertEquals("from-env", config.password());
    }

    @Test
    void testFilePasswordWinsOverEnvironment() {
        StatsConfig config = StatsConfig.read(properties(
                "stats.jdbc-url", "jdbc:postgresql://db/game",
                "stats.password", "from-file"), _ -> "from-env");
        assertEquals("from-file", config.password());
    }

    @Test
    void testUnparsablePoolSizeFallsBack() {
        StatsConfig config = StatsConfig.read(properties(
                "stats.jdbc-url", "jdbc:postgresql://db/game",
                "stats.pool-size", "many"), _ -> null);
        assertEquals(StatsConfig.DEFAULT_POOL_SIZE, config.poolSize());
    }

    @Test
    void testPoolSizeBelowOneDisables() {
        StatsConfig config = StatsConfig.read(properties(
                "stats.jdbc-url", "jdbc:postgresql://db/game",
                "stats.pool-size", "0"), _ -> null);
        assertSame(StatsConfig.DISABLED, config);
    }

    @Test
    void testInvalidSchemaFallsBack() {
        StatsConfig config = StatsConfig.read(properties(
                "stats.jdbc-url", "jdbc:postgresql://db/game",
                "stats.schema", "Drop Table;"), _ -> null);
        assertEquals(StatsConfig.DEFAULT_SCHEMA, config.schema());
    }

    @Test
    void testConstructorRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new StatsConfig("url", "", "", 0, "tamias_stats"));
        assertThrows(IllegalArgumentException.class, () -> new StatsConfig("url", "", "", 1, "Tamias"));
    }

    @Test
    void testToStringHidesPassword() {
        StatsConfig config = new StatsConfig("url", "user", "secret", 1, "tamias_stats");
        assertFalse(config.toString().contains("secret"));
    }

    @Test
    void testLoadWithoutFileIsDisabled(@TempDir Path directory) {
        assertSame(StatsConfig.DISABLED, StatsConfig.load(directory, _ -> null));
    }

    @Test
    void testLoadReadsFile(@TempDir Path directory) throws IOException {
        Files.writeString(directory.resolve("config.properties"), "stats.jdbc-url=jdbc:postgresql://db/game\n");
        assertEquals("jdbc:postgresql://db/game", StatsConfig.load(directory, _ -> null).jdbcUrl());
    }
}
