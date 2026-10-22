package net.theevilreaper.tamias.game.phase;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.event.AreaFinishBuildEvent;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class GroundBuildPhaseIntegrationTest {

    @Test
    void testRoundIsPreparedOnEveryStart(@NotNull Env env) {
        List<Class<? extends Event>> calledEvents = new ArrayList<>();
        trackEvent(env, calledEvents, AreaSpawnTriggerEvent.class);
        trackEvent(env, calledEvents, RoundPrepareEvent.class);
        trackEvent(env, calledEvents, RoundStartEvent.class);
        GroundBuildPhase phase = new GroundBuildPhase(() -> () -> {}, null);

        phase.start();
        phase.finish();
        phase.setFinished(false);
        phase.start();

        List<Class<? extends Event>> roundPreparation = List.of(AreaSpawnTriggerEvent.class, RoundPrepareEvent.class, RoundStartEvent.class);
        List<Class<? extends Event>> expected = new ArrayList<>(roundPreparation);
        expected.addAll(roundPreparation);
        assertEquals(expected, calledEvents);
    }

    @Test
    void testPhaseWaitsForTheTntAfterTheGround(@NotNull Env env) {
        CompletableFuture<Void> tntLanded = new CompletableFuture<>();
        AtomicBoolean tntDropped = new AtomicBoolean();
        GroundBuildPhase phase = new GroundBuildPhase(() -> () -> {}, () -> {
            tntDropped.set(true);
            return tntLanded;
        }, Duration.ZERO, Duration.ZERO);

        phase.start();
        env.tickWhile(() -> !tntDropped.get(), Duration.ofSeconds(5));
        assertTrue(tntDropped.get());

        EventDispatcher.call(new AreaFinishBuildEvent());
        assertFalse(phase.isFinished(), "The phase must wait until every TNT block has landed");

        tntLanded.complete(null);
        assertTrue(phase.isFinished());
    }

    @Test
    void testPhaseWaitsForTheGroundAfterTheTnt(@NotNull Env env) {
        AtomicBoolean tntDropped = new AtomicBoolean();
        GroundBuildPhase phase = new GroundBuildPhase(() -> () -> {}, () -> {
            tntDropped.set(true);
            return CompletableFuture.completedFuture(null);
        }, Duration.ZERO, Duration.ZERO);

        phase.start();
        env.tickWhile(() -> !tntDropped.get(), Duration.ofSeconds(5));
        assertFalse(phase.isFinished(), "The phase must wait until the ground is built");

        EventDispatcher.call(new AreaFinishBuildEvent());
        assertTrue(phase.isFinished());
    }

    @Test
    void testFinishCancelsTheDelayedPlacements(@NotNull Env env) {
        AtomicBoolean groundStarted = new AtomicBoolean();
        GroundBuildPhase phase = new GroundBuildPhase(() -> {
            groundStarted.set(true);
            return () -> {};
        }, () -> CompletableFuture.completedFuture(null), Duration.ofMillis(100), Duration.ZERO);

        phase.start();
        phase.finish();
        env.tickWhile(() -> !groundStarted.get(), Duration.ofMillis(500));

        assertFalse(groundStarted.get());
    }

    private <T extends Event> void trackEvent(@NotNull Env env, @NotNull List<Class<? extends Event>> calledEvents, @NotNull Class<T> eventClass) {
        env.process().eventHandler().addListener(EventListener.of(eventClass, _ -> calledEvents.add(eventClass)));
    }
}
