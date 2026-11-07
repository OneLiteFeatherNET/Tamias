package net.theevilreaper.tamias.common.config;

import net.minestom.server.utils.validate.Check;

/**
 * The settings of the bomber assist. A bomber who explodes close to a survivor without reaching them gets a slightly
 * bigger explosion radius for the next explosion. Only near misses count, so exploding next to nobody on purpose
 * doesn't grow the radius. The bonus is gone after a kill or when the last near miss is older than the decay.
 *
 * @param step             how much the radius grows per near miss, in blocks
 * @param maxBonus         the largest bonus a bomber can get, in blocks; {@code 0} turns the assist off
 * @param nearMissDistance how close a survivor must have been for an explosion to count as a near miss, in blocks
 * @param decaySeconds     after how many seconds without a new near miss the bonus is gone
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record BomberAssistConfig(double step, double maxBonus, double nearMissDistance, int decaySeconds) {

    /**
     * The default values: +0.25 blocks per near miss up to +0.5 (3.5 instead of 3 blocks, about a third more area),
     * a survivor within 6 blocks makes a near miss and the bonus is gone 30 seconds after the last near miss.
     */
    public static final BomberAssistConfig DEFAULT = new BomberAssistConfig(0.25, 0.5, 6.0, 30);

    /**
     * Validates the values.
     */
    public BomberAssistConfig {
        Check.argCondition(step < 0, "The assist step must not be negative");
        Check.argCondition(maxBonus < 0, "The max assist bonus must not be negative");
        Check.argCondition(nearMissDistance < 0, "The near miss distance must not be negative");
        Check.argCondition(decaySeconds < 0, "The assist decay must not be negative");
    }

    /**
     * Returns whether the assist can give a bonus at all.
     *
     * @return true if a near miss grows the radius
     */
    public boolean enabled() {
        return this.step > 0 && this.maxBonus > 0;
    }

    /**
     * Returns the bonus for the given number of near misses.
     *
     * @param nearMisses the near misses since the last kill
     * @return the bonus in blocks, never more than {@link #maxBonus()}
     */
    public double bonus(int nearMisses) {
        if (!enabled() || nearMisses <= 0) return 0;
        return Math.min(nearMisses * this.step, this.maxBonus);
    }
}
