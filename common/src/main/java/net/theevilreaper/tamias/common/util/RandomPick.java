package net.theevilreaper.tamias.common.util;

import org.jetbrains.annotations.Nullable;

import java.util.random.RandomGenerator;

/**
 * Picks random enum constants which never repeat the constant of the last pick, e.g. for a pattern or an animation
 * which changes every round.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RandomPick {

    private RandomPick() {
        // No instance allowed for a utility class
    }

    /**
     * Picks a random constant which differs from the previous one, so the same constant never comes twice in a row.
     * If there is only one constant, it is returned even if it was the previous one.
     *
     * @param values   all constants of the enum, as returned by its {@code values()} method
     * @param previous the constant of the last pick, null if there was none
     * @param random   the random generator to use
     * @param <E>      the type of the enum
     * @return the picked constant
     */
    public static <E extends Enum<E>> E nextDifferent(E[] values, @Nullable E previous, RandomGenerator random) {
        if (previous == null || values.length == 1) return values[random.nextInt(values.length)];
        // Skips the previous constant by drawing from the remaining ones and shifting past it
        int index = random.nextInt(values.length - 1);
        return values[index >= previous.ordinal() ? index + 1 : index];
    }
}
