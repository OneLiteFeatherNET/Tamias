package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.PotionEffect;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MicrotusExtension.class)
class EffectsTest {

    @Test
    void testExplodeEffect(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        assertNoEffects(player);

        Effects.setEffectForExplode(player);

        assertTrue(player.hasEffect(PotionEffect.NIGHT_VISION));

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplodeOrHitEffect(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);

        assertNoEffects(player);

        Effects.setExplodeOrHitEffect(player);

        assertTrue(player.hasEffect(PotionEffect.SLOWNESS));
        assertTrue(player.hasEffect(PotionEffect.BLINDNESS));

        env.destroyInstance(instance, true);
    }

    private void assertNoEffects(@NotNull Player player) {
        assertFalse(player.hasEffect(PotionEffect.BLINDNESS));
        assertFalse(player.hasEffect(PotionEffect.SLOWNESS));
        assertFalse(player.hasEffect(PotionEffect.NIGHT_VISION));
    }
}
