package net.theevilreaper.tamias.game.listener.round;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.FireworkRocketMeta;
import net.minestom.server.item.component.FireworkList;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.TestConnection;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.ColorData;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundEndListenerIntegrationTest extends TamiasPlayerTestBase {

    private static final BiConsumer<Team, Team> NO_CREDIT = (survivors, bombers) -> {};

    @Test
    void testRoundEndShowsBomberAsPlayer(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(bomberTeam, player);
        assertEquals(DisguiseState.TNT_BLOCK, ((TamiasPlayer) player).getDisguiseState());

        new RoundEndListener(teamService::getTeams, () -> {}, NO_CREDIT).accept(new RoundEndEvent());

        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());
        assertTrue(bomberTeam.getPlayers().isEmpty());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndResetsSpectator(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player player = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), player);
        SpectatorHelper.enable(player);

        new RoundEndListener(teamService::getTeams, () -> {}, NO_CREDIT).accept(new RoundEndEvent());

        assertFalse(SpectatorHelper.isSpectator(player));
        assertFalse(player.isAllowFlying());
        assertEquals(DisguiseState.NONE, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndRemovesStaminaBars(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance);
        Player bomber = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), survivor);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);
        StaminaService staminaService = new StaminaService();
        staminaService.createStaminaObjects(teamService);
        staminaService.start();
        assertNotNull(staminaService.getStaminaBar(survivor));
        assertNotNull(staminaService.getStaminaBar(bomber));

        new RoundEndListener(teamService::getTeams, staminaService::cleanUp, NO_CREDIT).accept(new RoundEndEvent());

        assertNull(staminaService.getStaminaBar(survivor));
        assertNull(staminaService.getStaminaBar(bomber));

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndCreditsBeforeTeamsAreCleared(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance);
        Player bomber = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        Team survivorTeam = teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow();
        Team bomberTeam = teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow();
        TeamHelper.addPlayerToTeam(survivorTeam, survivor);
        TeamHelper.addPlayerToTeam(bomberTeam, bomber);
        AtomicInteger survivorsAtCredit = new AtomicInteger(-1);
        AtomicInteger bombersAtCredit = new AtomicInteger(-1);

        new RoundEndListener(teamService::getTeams, () -> {}, (survivors, bombers) -> {
            survivorsAtCredit.set(survivors.getPlayers().size());
            bombersAtCredit.set(bombers.getPlayers().size());
        }).accept(new RoundEndEvent());

        assertEquals(1, survivorsAtCredit.get());
        assertEquals(1, bombersAtCredit.get());
        assertTrue(survivorTeam.getPlayers().isEmpty());
        assertTrue(bomberTeam.getPlayers().isEmpty());

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndAnnouncesTheWinner(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        TestConnection connection = env.createConnection();
        Player survivor = connection.connect(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), survivor);
        Collector<SystemChatPacket> messages = connection.trackIncoming(SystemChatPacket.class);

        new RoundEndListener(teamService::getTeams, () -> {}, NO_CREDIT).accept(new RoundEndEvent());

        messages.assertSingle();
        String message = PlainTextComponentSerializer.plainText().serialize(messages.collect().getFirst().message());
        assertTrue(message.contains("Survivors"), "The message should name the winner: " + message);

        env.destroyInstance(instance, true);
    }

    @Test
    void testRoundEndLaunchesFireworkInTheWinnerColor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player survivor = env.createPlayer(instance);
        Player bomber = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.SURVIVOR_KEY).orElseThrow(), survivor);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);

        new RoundEndListener(teamService::getTeams, () -> {}, NO_CREDIT).accept(new RoundEndEvent());

        List<Entity> fireworks = getFireworks(instance);
        assertEquals(2, fireworks.size());
        for (Entity firework : fireworks) {
            FireworkList fireworkList = ((FireworkRocketMeta) firework.getEntityMeta()).getFireworkInfo().get(DataComponents.FIREWORKS);
            assertNotNull(fireworkList);
            assertEquals(ColorData.GREEN.getColor(), fireworkList.explosions().getFirst().colors().getFirst());
        }

        env.destroyInstance(instance, true);
    }

    @Test
    void testFireworkExplodesAfterTheFlight(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        Player bomber = env.createPlayer(instance);
        TeamService teamService = TeamService.of();
        TeamHelper.loadTeams(2, teamService);
        TeamHelper.addPlayerToTeam(teamService.getTeam(GameConfig.BOMBER_KEY).orElseThrow(), bomber);

        new RoundEndListener(teamService::getTeams, () -> {}, NO_CREDIT).accept(new RoundEndEvent());
        Entity firework = getFireworks(instance).getFirst();

        env.tickWhile(() -> !firework.isRemoved(), Duration.ofSeconds(5));

        assertTrue(firework.isRemoved());

        env.destroyInstance(instance, true);
    }

    private @NotNull List<Entity> getFireworks(@NotNull Instance instance) {
        return instance.getEntities().stream()
                .filter(entity -> entity.getEntityType() == EntityType.FIREWORK_ROCKET)
                .toList();
    }
}
