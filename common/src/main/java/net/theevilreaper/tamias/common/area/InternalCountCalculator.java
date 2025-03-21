package net.theevilreaper.tamias.common.area;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

@ApiStatus.NonExtendable
public final class InternalCountCalculator {

    private InternalCountCalculator() {
    }

    /**
     * Calculates a fixed amount value based on the available positions count.
     *
     * @param amount                  the amount to calculate
     * @param availablePositionsCount the available positions count
     * @return the calculated amount
     */
    public static int fixedCalculator(int amount, int availablePositionsCount) {
        return ThreadLocalRandom.current().nextInt(amount, availablePositionsCount);
    }

    /**
     * Calculates the amount of TNT blocks to place in the game area.
     *
     * @param availablePositionsCount the amount of available positions
     * @return the amount of TNT blocks to place
     */
    public static int defaultTNTCalculator(@NotNull IntSupplier availablePositionsCount) {
        return Math.min(
                ThreadLocalRandom.current().nextInt(GameAreaHelper.MAX_TNT_AMOUNT - GameAreaHelper.MIN_TNT_AMOUNT)
                        + GameAreaHelper.MIN_TNT_AMOUNT,
                availablePositionsCount.getAsInt()
        );
    }

    public static int defaultSpecialBlocksCalculator(@NotNull IntSupplier availablePositionsCount) {
        int randomCount = ThreadLocalRandom.current().nextInt(
                GameAreaHelper.MAX_SPEED_BOOST_AMOUNT - GameAreaHelper.MIN_SPEED_BOOST_AMOUNT)
                + GameAreaHelper.MIN_SPEED_BOOST_AMOUNT;
        return Math.min(randomCount, availablePositionsCount.getAsInt());
    }
}
