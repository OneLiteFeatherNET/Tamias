package net.theevilreaper.tamias.game.phase;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private <T extends Event> void trackEvent(@NotNull Env env, @NotNull List<Class<? extends Event>> calledEvents, @NotNull Class<T> eventClass) {
        env.process().eventHandler().addListener(EventListener.of(eventClass, _ -> calledEvents.add(eventClass)));
    }
}
