package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnProtectionIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testEnableProtectsPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        SpawnProtection.enable(player);

        assertTrue(SpawnProtection.isProtected(player));

        env.destroyInstance(instance, true);
    }

    @Test
    void testDisableRemovesProtection(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        SpawnProtection.enable(player);

        SpawnProtection.disable(player);

        assertFalse(SpawnProtection.isProtected(player));
        assertFalse(player.hasTag(Tags.SPAWN_PROTECTION));

        env.destroyInstance(instance, true);
    }

    @Test
    void testProtectionEndsAfterDuration(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        player.setTag(Tags.SPAWN_PROTECTION, System.currentTimeMillis() - 1);

        assertFalse(SpawnProtection.isProtected(player));

        env.destroyInstance(instance, true);
    }

    @Test
    void testPlayerWithoutTagIsNotProtected(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        assertFalse(SpawnProtection.isProtected(player));

        env.destroyInstance(instance, true);
    }
}
