package net.theevilreaper.tamias.game.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MicrotusExtension.class)
@SuppressWarnings("java:S3252")
class EntityHelperIntegrationTest {

    @Test
    void testSwitchToTNT(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        Player player = env.createPlayer(instance, Pos.ZERO);

        assertEquals(EntityType.PLAYER.id(), player.getEntityType().id());

        EntityHelper.switchToTNT(player);

        assertEquals(EntityType.TNT.id(), player.getEntityType().id());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSwitchToPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();

        Player player = env.createPlayer(instance, Pos.ZERO);

        EntityHelper.switchToTNT(player);

        assertNotEquals(EntityType.PLAYER.id(), player.getEntityType().id());

        EntityHelper.switchToPlayer(player);

        assertEquals(EntityType.PLAYER.id(), player.getEntityType().id());
        env.destroyInstance(instance, true);
    }
}
