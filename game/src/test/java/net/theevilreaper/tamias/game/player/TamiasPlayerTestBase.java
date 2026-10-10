package net.theevilreaper.tamias.game.player;

import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for tests whose players must be {@link TamiasPlayer}s, e.g. everything that reaches
 * {@link net.theevilreaper.tamias.game.util.EntityHelper}.
 * <p>
 * Cyano creates a fresh server process per test method, so the provider is installed before each test.
 */
@ExtendWith(MicrotusExtension.class)
public abstract class TamiasPlayerTestBase {

    @BeforeEach
    void installTamiasPlayer(Env env) {
        env.process().connection().setPlayerProvider(TamiasPlayer::new);
    }
}
