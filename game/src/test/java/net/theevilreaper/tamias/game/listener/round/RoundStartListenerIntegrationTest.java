package net.theevilreaper.tamias.game.listener.round;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.game.round.RoundProvider;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import net.theevilreaper.tamias.game.scoreboard.GameScoreboard;
import net.theevilreaper.tamias.game.scoreboard.GameScoreboardAccess;
import net.theevilreaper.tamias.game.scoreboard.ScoreType;
import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.GamePhase;
import net.theevilreaper.xerus.api.phase.Phase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MicrotusExtension.class)
class RoundStartListenerIntegrationTest {

    @Test
    void testRoundStartUpdatesGameScoreboard(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        CyclicPhaseSeries<Phase> phaseSeries = new CyclicPhaseSeries<>("test");
        phaseSeries.add(new GamePhase("round") {
            @Override
            protected void onStart() {
                // Nothing to do on start
            }
        });
        phaseSeries.setMaxIterations(3);
        GameScoreboard gameScoreboard = new GameScoreboard();
        gameScoreboard.addViewer(player);

        new RoundStartListener(gameScoreboard, new RoundProvider(phaseSeries)).accept(new RoundStartEvent());

        Sidebar sidebar = Objects.requireNonNull(GameScoreboardAccess.sidebar(gameScoreboard, player));
        Sidebar.ScoreboardLine line = Objects.requireNonNull(sidebar.getLine(ScoreType.ROUND_TYPE.key().asString()));
        assertEquals("» 1 / 3", PlainTextComponentSerializer.plainText().serialize(line.getContent()));

        env.destroyInstance(instance, true);
    }
}
