package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.player.DisguiseState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BomberExplodeListenerIntegrationTest extends TamiasPlayerTestBase {

    private TeamService teamService;
    private List<Player> spawned;

    @BeforeEach
    void setUp(@NotNull Env env) {
        this.teamService = TeamService.of();
        TeamHelper.loadTeams(10, this.teamService);
        this.spawned = new ArrayList<>();
        env.process().eventHandler().addListener(
                EventListener.of(RoleToBomberChangeEvent.class, event -> this.spawned.add(event.getPlayer()))
        );
    }

    @Test
    void testExplosionHidesBomber(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player player = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        ((TamiasPlayer) player).setDisguise(DisguiseState.PRIMED_TNT);

        new BomberExplodeListener(this.teamService, () -> false).accept(new BomberExplodeEvent(player, player.getPosition().asVec()));

        assertEquals(DisguiseState.HIDDEN, ((TamiasPlayer) player).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionConvertsNearbySurvivors(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player nearSurvivor = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);
        Player farSurvivor = createPlayer(env, instance, new Pos(10, 40, 0), GameConfig.SURVIVOR_KEY);
        createPlayer(env, instance, new Pos(0, 40, 1), GameConfig.BOMBER_KEY);

        AtomicInteger roundEndChecks = new AtomicInteger();
        BomberExplodeListener listener = new BomberExplodeListener(this.teamService, () -> roundEndChecks.incrementAndGet() < 0);

        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(GameConfig.BOMBER_KEY.asString(), nearSurvivor.getTag(Tags.TEAM_KEY));
        assertEquals(GameConfig.SURVIVOR_KEY.asString(), farSurvivor.getTag(Tags.TEAM_KEY));
        assertEquals(List.of(nearSurvivor), this.spawned);
        assertEquals(1, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testLastSurvivorDoesNotSpawnWhenRoundEnds(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player lastSurvivor = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);

        new BomberExplodeListener(this.teamService, () -> true).accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(GameConfig.BOMBER_KEY.asString(), lastSurvivor.getTag(Tags.TEAM_KEY));
        assertEquals(List.of(), this.spawned);

        env.destroyInstance(instance, true);
    }

    @Test
    void testShotExplosionConvertsNoSurvivor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player survivor = createPlayer(env, instance, new Pos(1, 40, 0), GameConfig.SURVIVOR_KEY);

        AtomicInteger roundEndChecks = new AtomicInteger();
        BomberExplodeListener listener = new BomberExplodeListener(this.teamService, () -> roundEndChecks.incrementAndGet() < 0);

        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec(), true));

        assertEquals(GameConfig.SURVIVOR_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));
        assertEquals(List.of(), this.spawned);
        assertEquals(0, roundEndChecks.get());
        assertEquals(0, ((TamiasPlayer) bomber).getKills());
        assertEquals(DisguiseState.HIDDEN, ((TamiasPlayer) bomber).getDisguiseState());

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionGivesFeedback(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        TestConnection bomberConnection = env.createConnection();
        Player bomber = bomberConnection.connect(instance, new Pos(0, 40, 0));
        TeamHelper.addPlayerToTeam(TeamHelper.getTeam(this.teamService, GameConfig.BOMBER_KEY), bomber);
        createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);
        Collector<SoundEffectPacket> bomberSounds = bomberConnection.trackIncoming(SoundEffectPacket.class);
        Collector<SystemChatPacket> bomberMessages = bomberConnection.trackIncoming(SystemChatPacket.class);

        new BomberExplodeListener(this.teamService, () -> true).accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        bomberSounds.assertSingle(packet -> assertEquals(SoundEvent.ENTITY_ARROW_HIT_PLAYER, packet.soundEvent()));
        bomberMessages.assertSingle();

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionWithoutSurvivorsSkipsRoundEndCheck(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, new Pos(10, 40, 0), GameConfig.SURVIVOR_KEY);

        AtomicInteger roundEndChecks = new AtomicInteger();
        BomberExplodeListener listener = new BomberExplodeListener(this.teamService, () -> roundEndChecks.incrementAndGet() < 0);

        listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(List.of(), this.spawned);
        assertEquals(0, roundEndChecks.get());

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionCountsKillPerConvertedSurvivor(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);
        createPlayer(env, instance, new Pos(0, 40, 2), GameConfig.SURVIVOR_KEY);

        new BomberExplodeListener(this.teamService, () -> true).accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(2, ((TamiasPlayer) bomber).getKills());

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionWithoutSurvivorsCountsNoKill(@NotNull Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, new Pos(10, 40, 0), GameConfig.SURVIVOR_KEY);

        new BomberExplodeListener(this.teamService, () -> false).accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(0, ((TamiasPlayer) bomber).getKills());

        env.destroyInstance(instance, true);
    }

    private Player createPlayer(Env env, Instance instance, Pos pos, Key teamKey) {
        Player player = env.createPlayer(instance, pos);
        TeamHelper.addPlayerToTeam(TeamHelper.getTeam(this.teamService, teamKey), player);
        return player;
    }
}
