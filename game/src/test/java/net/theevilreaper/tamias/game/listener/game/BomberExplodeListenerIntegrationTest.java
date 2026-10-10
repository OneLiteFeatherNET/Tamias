package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BomberExplodeListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testExplosionHidesBomber(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        ((TamiasPlayer) player).setDisguise(DisguiseState.PRIMED_TNT);

        new BomberExplodeListener().accept(new BomberExplodeEvent(player, player.getPosition().asVec()));

        assertEquals(DisguiseState.HIDDEN, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }
}
