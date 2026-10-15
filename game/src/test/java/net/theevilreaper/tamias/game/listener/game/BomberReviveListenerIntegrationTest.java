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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BomberReviveListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testReviveStandsOnRemovedBlockAsTnt(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());
        ((TamiasPlayer) player).setDisguise(DisguiseState.HIDDEN);

        Pos randomTntPos = new Pos(5, 41, 5);
        Pos removedBlock = randomTntPos.add(0, 1, 0);
        instance.setBlock(removedBlock, Block.TNT);

        ExplodeBar explodeBar = (ExplodeBar) StaminaFactory.createExplodeBar(player);
        BomberReviveListener listener = new BomberReviveListener(_ -> explodeBar, () -> randomTntPos);

        listener.accept(new BomberRequireSpawnEvent(player, explodeBar));
        env.tick();

        assertEquals(Block.AIR, instance.getBlock(removedBlock));
        assertEquals(removedBlock.add(0.5, 0, 0.5), player.getPosition().withView(0f, 0f));
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }
}
