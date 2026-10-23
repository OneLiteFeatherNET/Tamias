package net.theevilreaper.tamias.game.util;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpectatorHelperIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testEnableTurnsPlayerIntoSpectator(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        player.getInventory().addItemStack(ItemStack.of(Material.TNT));
        player.addEffect(new Potion(PotionEffect.BLINDNESS, (byte) 1, Integer.MAX_VALUE));
        AttributeHelper.disableMovement(player);

        SpectatorHelper.enable(player);

        assertTrue(SpectatorHelper.isSpectator(player));
        assertEquals(GameMode.ADVENTURE, player.getGameMode());
        assertTrue(player.isAllowFlying());
        assertTrue(player.isFlying());
        assertEquals(DisguiseState.HIDDEN, ((TamiasPlayer) player).getDisguiseState());
        assertTrue(player.getInventory().getItemStack(0).isAir());
        assertTrue(player.getActiveEffects().isEmpty());
        assertTrue(player.getAttribute(Attribute.MOVEMENT_SPEED).getValue() > 0);

        env.destroyInstance(instance, true);
    }

    @Test
    void testDisableResetsSpectator(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        SpectatorHelper.enable(player);

        SpectatorHelper.disable(player);

        assertFalse(SpectatorHelper.isSpectator(player));
        assertFalse(player.isAllowFlying());
        assertFalse(player.isFlying());
        // Adventure keeps the survivors from punching the TNT blocks to find a bomber
        assertEquals(GameMode.ADVENTURE, player.getGameMode());

        env.destroyInstance(instance, true);
    }
}
