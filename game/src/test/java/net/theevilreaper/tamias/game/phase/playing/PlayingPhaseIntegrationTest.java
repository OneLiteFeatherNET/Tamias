package net.theevilreaper.tamias.game.phase.playing;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventListener;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
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
        List<RoundEndEvent> events = trackRoundEnd();
        PlayingPhase phase = new PlayingPhase(_ -> {}, () -> {}, HashMap::new);
        phase.start();

        phase.finish();

        assertEquals(List.of(new RoundEndEvent(false)), events);
    }

    @Test
    void testSkippedFinishAbortsTheRound(@NotNull Env env) {
        List<RoundEndEvent> events = trackRoundEnd();
        PlayingPhase phase = new PlayingPhase(_ -> {}, () -> {}, HashMap::new);
        phase.start();

        phase.setSkipping(true);
        phase.finish();

        assertEquals(List.of(new RoundEndEvent(true)), events);
    }

    private @NotNull List<RoundEndEvent> trackRoundEnd() {
        List<RoundEndEvent> events = new ArrayList<>();
        MinecraftServer.getGlobalEventHandler().addListener(EventListener.of(RoundEndEvent.class, events::add));
        return events;
    }
}
