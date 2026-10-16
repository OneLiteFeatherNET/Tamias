package net.theevilreaper.tamias.game.phase.playing;

import net.minestom.server.event.EventListener;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class PostPlayingPhaseIntegrationTest {

    @Test
    void testRoundIsCleanedUpOnFinish(@NotNull Env env) {
        AtomicInteger cleanupCalls = new AtomicInteger();
        env.process().eventHandler().addListener(EventListener.of(GameCleanupEvent.class, _ -> cleanupCalls.incrementAndGet()));
        PostPlayingPhase phase = new PostPlayingPhase();

        phase.start();
        phase.finish();

        assertEquals(1, cleanupCalls.get());
    }

    @Test
    void testNextRoundIsNotPreparedByThePhase(@NotNull Env env) {
        AtomicInteger prepareCalls = new AtomicInteger();
        env.process().eventHandler().addListener(EventListener.of(RoundPrepareEvent.class, _ -> prepareCalls.incrementAndGet()));
        env.process().eventHandler().addListener(EventListener.of(AreaSpawnTriggerEvent.class, _ -> prepareCalls.incrementAndGet()));
        PostPlayingPhase phase = new PostPlayingPhase();

        phase.start();
        for (int ticks = phase.getCurrentTicks(); ticks > 0; ticks--) {
            phase.setCurrentTicks(ticks);
            phase.onUpdate();
        }
        phase.finish();

        assertEquals(0, prepareCalls.get());
    }
}
