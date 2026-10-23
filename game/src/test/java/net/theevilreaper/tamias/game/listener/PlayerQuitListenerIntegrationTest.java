package net.theevilreaper.tamias.game.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PrePlayingPhase;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerQuitListenerIntegrationTest extends TamiasPlayerTestBase {

    private final AtomicInteger roundEndChecks = new AtomicInteger();
    private final AtomicInteger earlyGameFinishes = new AtomicInteger();

    @Test
    void testGameQuitRemovesPlayerFromTeamAndStaminaBar(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player bomber = env.createPlayer(instance);
        env.createPlayer(instance);
        env.createPlayer(instance);
        TeamService teamService = loadTeams();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);
        StaminaService staminaService = new StaminaService();
        staminaService.createStaminaObjects(teamService);
        staminaService.start();
        assertNotNull(staminaService.getStaminaBar(bomber));

        createListener(new PlayingPhase(_ -> {}, () -> {}, HashMap::new), teamService, staminaService)
                .accept(new PlayerDisconnectEvent(bomber));

        assertFalse(bomberTeam.hasPlayer(bomber));
        assertFalse(bomber.hasTag(Tags.TEAM_KEY));
        assertNull(staminaService.getStaminaBar(bomber));
        assertEquals(1, roundEndChecks.get());
        assertEquals(0, earlyGameFinishes.get());

        staminaService.cleanUp();
        env.destroyInstance(instance, true);
    }

    @Test
    void testQuitBeforeTheRoundStartsRemovesPlayerFromTeam(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance);
        env.createPlayer(instance);
        env.createPlayer(instance);
        TeamService teamService = loadTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);

        createListener(new PrePlayingPhase(teamService), teamService, new StaminaService())
                .accept(new PlayerDisconnectEvent(survivor));

        assertFalse(survivorTeam.hasPlayer(survivor));
        assertFalse(survivor.hasTag(Tags.TEAM_KEY));
        assertEquals(0, earlyGameFinishes.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testGameFinishesEarlyWhenTooFewPlayersAreLeft(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player leavingPlayer = env.createPlayer(instance);
        env.createPlayer(instance);
        TeamService teamService = loadTeams();

        createListener(new PlayingPhase(_ -> {}, () -> {}, HashMap::new), teamService, new StaminaService())
                .accept(new PlayerDisconnectEvent(leavingPlayer));

        assertEquals(1, earlyGameFinishes.get());
        assertEquals(0, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testQuitOfAPlayerKickedDuringTheConfigurationIsIgnored(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        connection.connect(instance);
        env.createPlayer(instance);
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);
        // A player who is kicked by the join listener is never spawned and has no instance
        Player kickedPlayer = new TamiasPlayer(new ConfigurationConnection(), new GameProfile(UUID.randomUUID(), "Kicked"));

        createListener(new PlayingPhase(_ -> {}, () -> {}, HashMap::new), loadTeams(), new StaminaService())
                .accept(new PlayerDisconnectEvent(kickedPlayer));

        messages.assertEmpty();
        assertEquals(0, roundEndChecks.get());
        assertEquals(0, earlyGameFinishes.get());

        env.destroyInstance(instance, true);
    }

    private @NotNull TeamService loadTeams() {
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(4, teamService);
        return teamService;
    }

    private @NotNull PlayerQuitListener createListener(
            @NotNull Phase phase,
            @NotNull TeamService teamService,
            @NotNull StaminaService staminaService
    ) {
        return new PlayerQuitListener(
                () -> phase,
                key -> teamService.getTeam(key).orElse(null),
                player -> staminaService.removeStaminaBar(player.getUuid()),
                roundEndChecks::incrementAndGet,
                _ -> {},
                earlyGameFinishes::incrementAndGet
        );
    }

    private static final class ConfigurationConnection extends PlayerConnection {

        @Override
        public void sendPacket(@NotNull SendablePacket packet) {
            // The player never reaches the game, so no packet is needed
        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress(0);
        }
    }
}
