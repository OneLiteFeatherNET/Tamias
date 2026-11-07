package net.theevilreaper.tamias.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameConfigReaderTest {

    @TempDir
    Path directory;

    private GameConfig read(String content) throws IOException {
        Files.writeString(this.directory.resolve("config.properties"), content);
        return new GameConfigReader(this.directory).getConfig();
    }

    @Test
    void testMissingFileHasNoSentryDsn() {
        assertNull(new GameConfigReader(this.directory).getConfig().sentryDsn(),
                "without a config file error reporting must stay off");
    }

    @Test
    void testFileWithoutSentryDsnKeepsReportingOff() throws IOException {
        assertNull(read("minPlayers=2\n").sentryDsn(), "an absent key means no DSN");
    }

    @Test
    void testReadsTheSentryDsn() throws IOException {
        assertEquals("https://key@sentry.example.com/1", read("sentryDsn=https://key@sentry.example.com/1\n").sentryDsn());
    }

    @Test
    void testBlankSentryDsnIsNull() throws IOException {
        assertNull(read("sentryDsn=\nminPlayers=2\n").sentryDsn(), "a blank DSN means reporting stays off");
    }

    @Test
    void testMissingAssistKeysUseTheDefault() throws IOException {
        assertEquals(BomberAssistConfig.DEFAULT, read("minPlayers=2\n").bomberAssist());
    }

    @Test
    void testReadsTheAssistSettings() throws IOException {
        GameConfig config = read("""
                bomberAssistStep=0.5
                bomberAssistMaxBonus=1.0
                bomberAssistNearMissDistance=8
                bomberAssistDecaySeconds=20
                """);

        assertEquals(new BomberAssistConfig(0.5, 1.0, 8, 20), config.bomberAssist());
    }
}
