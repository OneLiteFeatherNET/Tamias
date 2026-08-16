package net.theevilreaper.tamias.game.round;

import net.theevilreaper.aves.util.functional.VoidConsumer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the shared, round-scoped pool of Bomber spawn/respawn tickets.
 * Every self-detonation conversion, self-detonation respawn, and post-shot
 * respawn consumes exactly one ticket via {@link #tryConsume()}. Once the
 * pool is empty, no further Bomber spawns/respawns can happen and the round
 * ends with a Bomber-side loss.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BomberTicketService {

    private final AtomicInteger tickets;

    public BomberTicketService() {
        this.tickets = new AtomicInteger(0);
    }

    /**
     * Sizes the ticket pool for a new round.
     *
     * @param onlinePlayerCount the number of players online at round start
     * @param ticketMultiplier  tickets awarded per online player
     */
    public void start(int onlinePlayerCount, int ticketMultiplier) {
        this.tickets.set(onlinePlayerCount * ticketMultiplier);
    }

    /**
     * Attempts to consume one ticket from the pool.
     *
     * @return {@code true} if a ticket was available and consumed, {@code false} if the pool was already empty
     */
    public boolean tryConsume() {
        return this.tickets.getAndUpdate(current -> current > 0 ? current - 1 : current) > 0;
    }

    /**
     * Attempts to consume one ticket from the pool and always runs the round-end check
     * afterward, since exhausting the pool here is itself a round-end condition.
     *
     * @param roundEndCheck invoked unconditionally after the consume attempt
     * @return {@code true} if a ticket was available and consumed, {@code false} if the pool was already empty
     */
    public boolean tryConsume(VoidConsumer roundEndCheck) {
        boolean ticketAvailable = tryConsume();
        roundEndCheck.apply();
        return ticketAvailable;
    }

    /**
     * Returns whether the ticket pool is exhausted.
     *
     * @return {@code true} if no tickets remain
     */
    public boolean isEmpty() {
        return this.tickets.get() <= 0;
    }

    /**
     * Returns the number of tickets currently remaining in the pool.
     *
     * @return the remaining ticket count
     */
    public int getRemaining() {
        return this.tickets.get();
    }
}
