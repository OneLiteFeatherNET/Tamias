package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class GameScoreboardIntegrationTest {

    private static GameScoreboard gameScoreboard;

    @BeforeAll
    static void setup() {
        gameScoreboard = new GameScoreboard();
        gameScoreboard.initDefaults();
    }

    @Test
    void testInvalidScoreTypeUsage() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> gameScoreboard.updateScore(ScoreType.MAP_TYPE, Component.empty()),
                        "Cannot update map score in GameScoreboard"
        );
    }
}
