package net.theevilreaper.tamias.common.area;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameAreaHelperTest {

    @ParameterizedTest
    @CsvSource({"2, 22", "8, 58", "13, 88"})
    void testTntAmountGrowsWithThePlayers(int players, int expected) {
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            int amount = GameAreaHelper.tntAmount(players, random);
            assertTrue(Math.abs(amount - expected) <= GameAreaHelper.TNT_AMOUNT_VARIANCE,
                    players + " players got " + amount + " TNT, expected about " + expected);
        }
    }

    @Test
    void testTntAmountIsPositiveWithoutPlayers() {
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            assertTrue(GameAreaHelper.tntAmount(0, random) > 0);
        }
    }
}
