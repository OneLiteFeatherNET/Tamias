package net.theevilreaper.tamias.game.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("java:S3252")
class EntityHelperIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testSwitchToTNT(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        EntityHelper.switchToTNT(player);

        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());
        assertEquals(EntityType.PLAYER, player.getEntityType());

        env.destroyInstance(instance, true);
    }

    @Test
    void testPrime(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        EntityHelper.switchToTNT(player);
        EntityHelper.prime(player);

        assertEquals(DisguiseState.PRIMED_TNT, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testHide(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        EntityHelper.prime(player);
        EntityHelper.hide(player);

        assertEquals(DisguiseState.HIDDEN, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSwitchToPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);

        EntityHelper.switchToTNT(player);
        EntityHelper.switchToPlayer(player);

        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());
        assertEquals(EntityType.PLAYER, player.getEntityType());

        env.destroyInstance(instance, true);
    }
}
