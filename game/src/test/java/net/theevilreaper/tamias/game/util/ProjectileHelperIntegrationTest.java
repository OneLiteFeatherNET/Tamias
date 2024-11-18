package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class ProjectileHelperIntegrationTest {

    @Test
    void testCreationWithoutAPlayerInstance(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        // We need a null reference on the instance to check if the method throws an exception
        player.remove(true);

        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> ProjectileHelper.spawnProjectile(player, player1 -> null),
                "Can't shoot without a instance from a player"
        );
    }

    @Test
    void testProjectileCreation(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        EntityProjectile projectile = ProjectileHelper.createProjectile(player);
        assertNotNull(projectile);
        assertEquals(EntityType.EGG, projectile.getEntityType());

        assertInstanceOf(ThrownEggMeta.class, projectile.getEntityMeta());
        assertTrue(projectile.getEntityMeta().isOnFire());

        assertEquals(1, instance.getEntities().size());
    }
}
