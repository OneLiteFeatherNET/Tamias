package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.testing.Env;
import net.minestom.testing.extension.MicrotusExtension;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.bomber.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.round.BomberTicketService;
import net.theevilreaper.tamias.game.stamina.ExplodeBar;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MicrotusExtension.class)
class BomberReviveListenerTest {

    @Test
    void testRespawnProceedsWhenTicketAvailable(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        player.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);
        assertInstanceOf(ExplodeBar.class, staminaBar);
        ExplodeBar explodeBar = (ExplodeBar) staminaBar;

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 1);
        AtomicInteger roundEndChecks = new AtomicInteger(0);

        Pos spawnPos = new Pos(5, 40, 5);
        BomberReviveListener listener = new BomberReviveListener(p -> explodeBar, () -> spawnPos, ticketService, roundEndChecks::incrementAndGet);

        BomberRequireSpawnEvent event = new BomberRequireSpawnEvent(player, explodeBar);
        listener.accept(event);

        assertFalse(event.isCancelled());
        assertTrue(ticketService.isEmpty());
        assertEquals(1, roundEndChecks.get());
        assertTrue(Arrays.stream(player.getInventory().getItemStacks())
                .anyMatch(stack -> stack.material() == Material.TNT));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRespawnCancelledWhenTicketsExhausted(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        player.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);
        ExplodeBar explodeBar = (ExplodeBar) staminaBar;

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 0);
        AtomicInteger roundEndChecks = new AtomicInteger(0);

        Pos spawnPos = new Pos(5, 40, 5);
        BomberReviveListener listener = new BomberReviveListener(p -> explodeBar, () -> spawnPos, ticketService, roundEndChecks::incrementAndGet);

        BomberRequireSpawnEvent event = new BomberRequireSpawnEvent(player, explodeBar);
        listener.accept(event);

        assertTrue(event.isCancelled());
        assertTrue(ticketService.isEmpty());
        assertEquals(1, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testTicketPreservedWhenSpawnPosMissing(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        player.setTag(Tags.TEAM_KEY, GameConfig.BOMBER_KEY.asString());

        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);
        ExplodeBar explodeBar = (ExplodeBar) staminaBar;

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 1);
        AtomicInteger roundEndChecks = new AtomicInteger(0);

        BomberReviveListener listener = new BomberReviveListener(p -> explodeBar, () -> null, ticketService, roundEndChecks::incrementAndGet);

        BomberRequireSpawnEvent event = new BomberRequireSpawnEvent(player, explodeBar);
        listener.accept(event);

        assertTrue(event.isCancelled());
        assertFalse(ticketService.isEmpty());
        assertEquals(1, ticketService.getRemaining());
        assertEquals(0, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testNonBomberIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance, Pos.ZERO);
        player.setTag(Tags.TEAM_KEY, GameConfig.SURVIVOR_KEY.asString());

        StaminaBar staminaBar = StaminaFactory.createExplodeBar(player);
        ExplodeBar explodeBar = (ExplodeBar) staminaBar;

        BomberTicketService ticketService = new BomberTicketService();
        ticketService.start(1, 1);
        AtomicInteger roundEndChecks = new AtomicInteger(0);

        BomberReviveListener listener = new BomberReviveListener(p -> explodeBar, () -> Pos.ZERO, ticketService, roundEndChecks::incrementAndGet);

        BomberRequireSpawnEvent event = new BomberRequireSpawnEvent(player, explodeBar);
        listener.accept(event);

        assertFalse(event.isCancelled());
        assertEquals(1, ticketService.getRemaining());
        assertEquals(0, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }
}
