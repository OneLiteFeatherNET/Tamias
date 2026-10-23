package net.theevilreaper.tamias.game.phase.playing;

import net.minestom.server.event.EventListener;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.event.RoundInGameEvent;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class PlayingPhaseIntegrationTest {

    @Test
    void testRegularFinishEndsTheRound(@NotNull Env env) {
        List<RoundEndEvent> events = trackRoundEnd(env);
        PlayingPhase phase = new PlayingPhase(300, _ -> {}, () -> {}, HashMap::new);
        phase.start();

        phase.finish();

        assertEquals(List.of(new RoundEndEvent(false)), events);
    }

    @Test
    void testRoundUsesTheGivenPlayingTime(@NotNull Env env) {
        List<Integer> times = new ArrayList<>();
        PlayingPhase phase = new PlayingPhase(120, times::add, () -> {}, HashMap::new);

        phase.start();

        assertEquals(120, phase.getCurrentTicks());
        assertEquals(List.of(120), times);
    }

    @Test
    void testSkippedFinishAbortsTheRound(@NotNull Env env) {
        List<RoundEndEvent> events = trackRoundEnd(env);
        PlayingPhase phase = new PlayingPhase(300, _ -> {}, () -> {}, HashMap::new);
        phase.start();

        phase.setSkipping(true);
        phase.finish();

        assertEquals(List.of(new RoundEndEvent(true)), events);
    }

    @Test
    void testStartSignalsTheRoundInGameOnce(@NotNull Env env) {
        List<RoundInGameEvent> events = new ArrayList<>();
        env.process().eventHandler().addListener(EventListener.of(RoundInGameEvent.class, events::add));
        PlayingPhase phase = new PlayingPhase(300, _ -> {}, () -> {}, HashMap::new);

        phase.start();

        assertEquals(1, events.size(), "starting the phase must signal the round in game exactly once");
    }

    @Test
    void testRoundInGameIsSignaledAfterTheGameLogicStarted(@NotNull Env env) {
        List<String> order = new ArrayList<>();
        env.process().eventHandler().addListener(EventListener.of(RoundInGameEvent.class, _ -> order.add("signal")));
        PlayingPhase phase = new PlayingPhase(300, _ -> {}, () -> order.add("logic"), HashMap::new);

        phase.start();

        assertEquals(List.of("logic", "signal"), order, "the signal must follow the game logic start");
    }

    private @NotNull List<RoundEndEvent> trackRoundEnd(Env env) {
        List<RoundEndEvent> events = new ArrayList<>();
        env.process().eventHandler().addListener(EventListener.of(RoundEndEvent.class, events::add));
        return events;
    }
}
