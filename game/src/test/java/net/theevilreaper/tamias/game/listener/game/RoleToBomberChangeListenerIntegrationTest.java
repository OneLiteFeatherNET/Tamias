package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.SpawnProtection;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleToBomberChangeListenerIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testConvertedPlayerStandsOnRemovedTntBlock(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        StaminaService staminaService = new StaminaService();
        staminaService.add(player.getUuid(), StaminaFactory.createShootBar(player));

        Pos tntBlock = new Pos(5, 42, 5);
        instance.setBlock(tntBlock, Block.TNT);

        AtomicBoolean roundEndChecked = new AtomicBoolean();
        RoleToBomberChangeListener listener = new RoleToBomberChangeListener(
                teamService, staminaService, () -> tntBlock, () -> roundEndChecked.set(true)
        );
        // A running player must not keep their momentum and slide away from the TNT position
        player.setVelocity(new Vec(10, 5, 10));
        listener.accept(new RoleToBomberChangeEvent(player));
        assertEquals(Vec.ZERO, player.getVelocity());
        env.tick();

        assertEquals(Block.AIR, instance.getBlock(tntBlock));
        assertEquals(tntBlock.add(0.5, 0, 0.5), player.getPosition().withView(0f, 0f));
        assertInstanceOf(ExplodeBar.class, staminaService.getStaminaBar(player));
        assertFalse(SpectatorHelper.isSpectator(player));
        assertTrue(SpawnProtection.isProtected(player));
        assertFalse(roundEndChecked.get());

        staminaService.cleanUp();
        env.destroyInstance(instance, true);
    }

    @Test
    void testConvertedPlayerBecomesSpectatorWithoutFreeTnt(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, new Pos(0, 40, 0));
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        StaminaService staminaService = new StaminaService();
        staminaService.add(player.getUuid(), StaminaFactory.createShootBar(player));

        AtomicBoolean roundEndChecked = new AtomicBoolean();
        RoleToBomberChangeListener listener = new RoleToBomberChangeListener(
                teamService, staminaService, () -> null, () -> roundEndChecked.set(true)
        );
        listener.accept(new RoleToBomberChangeEvent(player));

        assertTrue(SpectatorHelper.isSpectator(player));
        assertNull(staminaService.getStaminaBar(player));
        assertTrue(roundEndChecked.get());

        env.destroyInstance(instance, true);
    }
}
