package net.theevilreaper.tamias.phase;

import de.icevizion.xerus.api.phase.TimedPhase;

import java.time.temporal.ChronoUnit;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public final class PlayingPhase extends TimedPhase {

    public PlayingPhase() {
        super("GamePhase", ChronoUnit.SECONDS, 1);
    }

    @Override
    protected void onFinish() {

    }

    @Override
    public void onUpdate() {

    }
}
