package net.theevilreaper.tamias.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

@FunctionalInterface
public interface CountCalculator {

    /**
     * Calculates the number of special blocks to place in the game area.
     *
     * @param availablePositionsCount the amount of available positions
     * @return the number of special blocks to place
     */
    int calculateSpecialBlockCount(@NotNull IntSupplier availablePositionsCount);

}
