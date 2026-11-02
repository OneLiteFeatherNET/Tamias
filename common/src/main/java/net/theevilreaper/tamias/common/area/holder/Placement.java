package net.theevilreaper.tamias.common.area.holder;

import net.theevilreaper.tamias.common.ground.GroundData;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a placement which can be triggered.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Placement {

    /**
     * Clears the placement.
     */
    void clear();

    /**
     * Triggers the placement.
     *
     * @param groundData the data which defines the blocks to place
     * @return a future which completes once the placement is done, it doesn't complete when the placement is stopped
     */
    CompletableFuture<Void> triggerPlacement(GroundData groundData);
}
