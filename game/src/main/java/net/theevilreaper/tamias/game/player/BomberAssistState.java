package net.theevilreaper.tamias.game.player;

import net.theevilreaper.tamias.common.config.BomberAssistConfig;

/**
 * Counts the near misses of a bomber for the bomber assist, see {@link BomberAssistConfig}.
 * The near misses are gone after a kill, at the end of a round or when the last one is older than the decay.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BomberAssistState {

    private int nearMisses;
    private long lastNearMissMillis;

    /**
     * Counts a near miss.
     *
     * @param config    the assist settings, for the decay of older near misses
     * @param nowMillis the current time in milliseconds
     */
    public void recordNearMiss(BomberAssistConfig config, long nowMillis) {
        this.nearMisses = nearMisses(config, nowMillis) + 1;
        this.lastNearMissMillis = nowMillis;
    }

    /**
     * Returns the near misses which still count.
     *
     * @param config    the assist settings, for the decay
     * @param nowMillis the current time in milliseconds
     * @return the near misses, {@code 0} if the last one is older than the decay
     */
    public int nearMisses(BomberAssistConfig config, long nowMillis) {
        if (this.nearMisses == 0) return 0;
        boolean expired = nowMillis - this.lastNearMissMillis > config.decaySeconds() * 1000L;
        return expired ? 0 : this.nearMisses;
    }

    /**
     * Returns the radius bonus the bomber has right now.
     *
     * @param config    the assist settings
     * @param nowMillis the current time in milliseconds
     * @return the bonus in blocks
     */
    public double bonus(BomberAssistConfig config, long nowMillis) {
        return config.bonus(nearMisses(config, nowMillis));
    }

    /**
     * Forgets every near miss, e.g. after a kill.
     */
    public void reset() {
        this.nearMisses = 0;
        this.lastNearMissMillis = 0;
    }
}
