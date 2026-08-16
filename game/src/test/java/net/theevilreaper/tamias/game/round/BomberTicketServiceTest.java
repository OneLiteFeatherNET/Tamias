package net.theevilreaper.tamias.game.round;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BomberTicketServiceTest {

    @Test
    void testStartSetsPoolFromMultiplier() {
        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(10, 5);

        assertEquals(50, ticketService.getRemaining());
        assertFalse(ticketService.isEmpty());
    }

    @Test
    void testTryConsumeDecrementsUntilEmpty() {
        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 2);

        assertTrue(ticketService.tryConsume());
        assertEquals(1, ticketService.getRemaining());

        assertTrue(ticketService.tryConsume());
        assertEquals(0, ticketService.getRemaining());
        assertTrue(ticketService.isEmpty());

        assertFalse(ticketService.tryConsume());
        assertEquals(0, ticketService.getRemaining());
    }

    @Test
    void testConcurrentTryConsumeNeverGoesNegative() throws InterruptedException {
        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 10);

        int threadCount = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                if (ticketService.tryConsume()) {
                    successCount.incrementAndGet();
                }
                latch.countDown();
            }).start();
        }

        latch.await();

        assertEquals(10, successCount.get());
        assertEquals(0, ticketService.getRemaining());
        assertTrue(ticketService.isEmpty());
    }
}
