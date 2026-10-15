package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BomberExplodeListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testExplosionHidesBomber(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        ((TamiasPlayer) player).setDisguise(DisguiseState.PRIMED_TNT);

        new BomberExplodeListener(_ -> {}).accept(new BomberExplodeEvent(player, player.getPosition().asVec()));

        assertEquals(DisguiseState.HIDDEN, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionConvertsNearbySurvivors(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY.asString());
        Player nearSurvivor = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY.asString());
        createPlayer(env, instance, new Pos(10, 40, 0), GameConfig.SURVIVOR_KEY.asString());
        createPlayer(env, instance, new Pos(0, 40, 1), GameConfig.BOMBER_KEY.asString());

        List<Player> converted = new ArrayList<>();
        BomberExplodeListener listener = new BomberExplodeListener(converted::add);

        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(List.of(nearSurvivor), converted);

        env.destroyInstance(instance, true);
    }

    private Player createPlayer(Env env, Instance instance, Pos pos, String teamKey) {
        Player player = env.createPlayer(instance, pos);
        player.setTag(Tags.TEAM_KEY, teamKey);
        return player;
    }
}
