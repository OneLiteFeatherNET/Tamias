package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.common.config.BomberAssistConfig;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.player.BomberAssistState;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BomberAssistIntegrationTest extends TamiasPlayerTestBase {

    private static final BomberAssistConfig CONFIG = new BomberAssistConfig(0.25, 0.5, 6, 30);
    // Just outside the base radius of 3 blocks, inside the radius after two near misses
    private static final Pos JUST_OUT_OF_RANGE = new Pos(3.3, 40, 0);

    private TeamService teamService;
    private AtomicLong clock;
    private BomberExplodeListener listener;

    @BeforeEach
    void setUp() {
        this.teamService = TeamService.of();
        TeamHelper.loadTeams(10, this.teamService);
        this.clock = new AtomicLong(1000);
        this.listener = new BomberExplodeListener(this.teamService, () -> true, CONFIG, this.clock::get);
    }

    @Test
    void testNearMissesGrowTheRadiusUntilTheSurvivorIsHit(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player survivor = createPlayer(env, instance, JUST_OUT_OF_RANGE, GameConfig.SURVIVOR_KEY);

        explode(bomber, false);
        explode(bomber, false);
        assertEquals(GameConfig.SURVIVOR_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));
        assertEquals(2, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        explode(bomber, false);
        assertEquals(GameConfig.BOMBER_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));

        env.destroyInstance(instance, true);
    }

    @Test
    void testKillRemovesTheBonus(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);
        assist(bomber).recordNearMiss(CONFIG, this.clock.get());

        explode(bomber, false);

        assertEquals(0, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testExplosionFarFromSurvivorsIsNoNearMiss(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, new Pos(10, 40, 0), GameConfig.SURVIVOR_KEY);

        explode(bomber, false);

        assertEquals(0, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testShotNextToASurvivorIsANearMiss(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player survivor = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);

        explode(bomber, true);

        assertEquals(GameConfig.SURVIVOR_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));
        assertEquals(1, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testShotFarFromSurvivorsIsNoNearMiss(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, new Pos(10, 40, 0), GameConfig.SURVIVOR_KEY);

        explode(bomber, true);

        assertEquals(0, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testBonusDecaysWithoutANewNearMiss(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player survivor = createPlayer(env, instance, JUST_OUT_OF_RANGE, GameConfig.SURVIVOR_KEY);
        assist(bomber).recordNearMiss(CONFIG, this.clock.get());
        assist(bomber).recordNearMiss(CONFIG, this.clock.get());

        this.clock.addAndGet(31_000);
        explode(bomber, false);

        assertEquals(GameConfig.SURVIVOR_KEY.asString(), survivor.getTag(Tags.TEAM_KEY));
        // The explosion itself was a near miss again, the old ones are gone
        assertEquals(1, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testDisabledAssistCountsNothing(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        createPlayer(env, instance, JUST_OUT_OF_RANGE, GameConfig.SURVIVOR_KEY);
        BomberExplodeListener disabled = new BomberExplodeListener(this.teamService, () -> true,
                new BomberAssistConfig(0.25, 0, 6, 30), this.clock::get);

        disabled.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertEquals(0, assist(bomber).nearMisses(CONFIG, this.clock.get()));

        env.destroyInstance(instance, true);
    }

    @Test
    void testOnlyAGrownExplosionShowsTheRing(@NotNull Env env) {
        Instance instance = createInstance(env);
        TestConnection survivorConnection = env.createConnection();
        Player survivor = survivorConnection.connect(instance, new Pos(10, 40, 0));
        TeamHelper.addPlayerToTeam(TeamHelper.getTeam(this.teamService, GameConfig.SURVIVOR_KEY), survivor);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);

        Collector<ParticlePacket> normal = survivorConnection.trackIncoming(ParticlePacket.class);
        explode(bomber, false);
        assertFalse(normal.collect().stream().anyMatch(packet -> packet.particle().equals(Particle.FLAME)));

        assist(bomber).recordNearMiss(CONFIG, this.clock.get());
        Collector<ParticlePacket> grown = survivorConnection.trackIncoming(ParticlePacket.class);
        explode(bomber, false);
        assertTrue(grown.collect().stream().anyMatch(packet -> packet.particle().equals(Particle.FLAME)));

        env.destroyInstance(instance, true);
    }

    private void explode(Player bomber, boolean shot) {
        this.listener.accept(new BomberExplodeEvent(bomber, bomber.getPosition().asVec(), shot));
    }

    private static BomberAssistState assist(Player bomber) {
        return ((TamiasPlayer) bomber).getBomberAssist();
    }

    private static Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        return instance;
    }

    private Player createPlayer(Env env, Instance instance, Pos pos, Key teamKey) {
        Player player = env.createPlayer(instance, pos);
        TeamHelper.addPlayerToTeam(TeamHelper.getTeam(this.teamService, teamKey), player);
        return player;
    }
}
