package net.theevilreaper.tamias.round;

import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.config.GameConfig;
import net.theevilreaper.tamias.round.events.RoundFinishEvent;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class RoundData {

    private int tntCount;

    public RoundData() { }

    public int calculateTNTCount() {
        return 1;
    }

    public void decreaseTNTCount() {
        if (--tntCount == 0) {
            EventDispatcher.call(new RoundFinishEvent(GameConfig.SURVIVOR_ID));
        }
    }

    public boolean hasTNT() {
        return this.tntCount > 0;
    }
}
