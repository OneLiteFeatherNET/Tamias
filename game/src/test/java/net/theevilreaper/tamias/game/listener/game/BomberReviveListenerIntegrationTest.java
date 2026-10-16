package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BomberReviveListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testReviveStandsOnRemovedBlockAsTnt(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());
        ((TamiasPlayer) player).setDisguise(DisguiseState.HIDDEN);

        Pos tntBlock = new Pos(5, 42, 5);
        instance.setBlock(tntBlock, Block.TNT);

        AtomicInteger supplierCalls = new AtomicInteger();
        AtomicBoolean roundEndChecked = new AtomicBoolean();
        ExplodeBar explodeBar = (ExplodeBar) StaminaFactory.createExplodeBar(player);
        BomberReviveListener listener = new BomberReviveListener(
                _ -> explodeBar,
                () -> {
                    supplierCalls.incrementAndGet();
                    return tntBlock;
                },
                _ -> {},
                () -> roundEndChecked.set(true)
        );

        listener.accept(new BomberRequireSpawnEvent(player, explodeBar));
        env.tick();

        assertEquals(1, supplierCalls.get(), "A respawn must only consume one TNT");
        assertEquals(Block.AIR, instance.getBlock(tntBlock));
        assertEquals(tntBlock.add(0.5, 0, 0.5), player.getPosition().withView(0f, 0f));
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());
        assertFalse(roundEndChecked.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testBomberBecomesSpectatorWithoutFreeTnt(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        AtomicBoolean staminaRemoved = new AtomicBoolean();
        AtomicBoolean roundEndChecked = new AtomicBoolean();
        ExplodeBar explodeBar = (ExplodeBar) StaminaFactory.createExplodeBar(player);
        BomberReviveListener listener = new BomberReviveListener(
                _ -> explodeBar,
                () -> null,
                _ -> staminaRemoved.set(true),
                () -> roundEndChecked.set(true)
        );

        BomberRequireSpawnEvent event = new BomberRequireSpawnEvent(player, explodeBar);
        listener.accept(event);

        assertTrue(event.isCancelled());
        assertTrue(SpectatorHelper.isSpectator(player));
        assertTrue(staminaRemoved.get());
        assertTrue(roundEndChecked.get());

        env.destroyInstance(instance, true);
    }
}
