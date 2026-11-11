package net.theevilreaper.tamias.game.round;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.RoundRole;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundWinsIntegrationTest extends TamiasPlayerTestBase {

    @Test
    void testSurvivorWinCreditsRemainingStartSurvivors(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer survivorA = createPlayer(env, instance);
        TamiasPlayer survivorB = createPlayer(env, instance);
        TamiasPlayer bomber = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivorA);
        TeamHelper.addPlayerToTeam(survivorTeam, survivorB);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        TeamHelper.switchToTNTTeam(teamService, survivorB);
        RoundWins.credit(survivorTeam, bomberTeam);

        assertEquals(1, survivorA.getRoundWins());
        assertEquals(0, survivorB.getRoundWins());
        assertEquals(0, bomber.getRoundWins());

        env.destroyInstance(instance, true);
    }

    @Test
    void testBomberWinCreditsOnlyStartBombers(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer survivor = createPlayer(env, instance);
        TamiasPlayer bomber = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        TeamHelper.switchToTNTTeam(teamService, survivor);
        RoundWins.credit(survivorTeam, bomberTeam);

        assertEquals(1, bomber.getRoundWins());
        assertEquals(0, survivor.getRoundWins());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSpectatingStartBomberGetsWin(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer survivor = createPlayer(env, instance);
        TamiasPlayer bomber = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        TeamHelper.switchToTNTTeam(teamService, survivor);
        SpectatorHelper.enable(bomber);
        RoundWins.credit(survivorTeam, bomberTeam);

        assertEquals(1, bomber.getRoundWins());

        env.destroyInstance(instance, true);
    }

    @Test
    void testSnapshotOverwritesPreviousRole(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TamiasPlayer player = createPlayer(env, instance);
        TeamHelper.addPlayerToTeam(bomberTeam, player);
        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        TeamHelper.removePlayerFromTeam(bomberTeam, player);

        TeamHelper.addPlayerToTeam(survivorTeam, player);
        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        assertEquals(RoundRole.SURVIVOR, player.getStartRole());

        TeamHelper.switchToTNTTeam(teamService, player);
        RoundWins.credit(survivorTeam, bomberTeam);

        assertEquals(0, player.getRoundWins());

        env.destroyInstance(instance, true);
    }

    @Test
    void testConvertedSurvivorIsToldWhyTheWinDoesNotCount(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TeamService teamService = createTeams();
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TestConnection survivorConnection = env.createConnection();
        TestConnection bomberConnection = env.createConnection();
        TamiasPlayer survivor = (TamiasPlayer) survivorConnection.connect(instance);
        TamiasPlayer bomber = (TamiasPlayer) bomberConnection.connect(instance);
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);

        RoundWins.snapshotRoles(survivorTeam, bomberTeam);
        TeamHelper.moveToTNTTeam(teamService, survivor);
        Collector<SystemChatPacket> survivorMessages = survivorConnection.trackIncoming(SystemChatPacket.class);
        Collector<SystemChatPacket> bomberMessages = bomberConnection.trackIncoming(SystemChatPacket.class);
        RoundWins.credit(survivorTeam, bomberTeam);

        List<SystemChatPacket> packets = survivorMessages.collect();
        assertEquals(1, packets.size());
        String text = PlainTextComponentSerializer.plainText().serialize(packets.getFirst().message());
        assertTrue(text.contains("started this round as a survivor"), text);
        bomberMessages.assertEmpty();

        env.destroyInstance(instance, true);
    }

    private @NotNull TeamService createTeams() {
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(4, teamService);
        return teamService;
    }

    private @NotNull TamiasPlayer createPlayer(@NotNull Env env, @NotNull Instance instance) {
        return (TamiasPlayer) env.createPlayer(instance);
    }
}
