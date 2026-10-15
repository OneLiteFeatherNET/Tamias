package net.theevilreaper.tamias.game.util;

import ca.atlasengine.projectiles.AbstractProjectile;
import ca.atlasengine.projectiles.entities.ThrownItemProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.util.Tags;
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

        AbstractProjectile projectile = ProjectileHelper.createProjectile(player);
        assertNotNull(projectile);
        assertInstanceOf(ThrownItemProjectile.class, projectile);
        assertEquals(EntityType.EGG, projectile.getEntityType());
        assertSame(player, projectile.getShooter());

        assertInstanceOf(ThrownEggMeta.class, projectile.getEntityMeta());
        assertTrue(projectile.getEntityMeta().isOnFire());

        // The projectile is only spawned when it gets shot
        assertEquals(1, instance.getEntities().size());
    }

    @Test
    void testProjectileSpawn(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        AbstractProjectile projectile = ProjectileHelper.createProjectile(player);
        ProjectileHelper.spawnProjectile(player, ignored -> projectile);

        assertEquals(player.getUuid(), projectile.getTag(Tags.SHOOTER_ID));
        assertSame(instance, projectile.getInstance());
        assertEquals(2, instance.getEntities().size());
    }
}
