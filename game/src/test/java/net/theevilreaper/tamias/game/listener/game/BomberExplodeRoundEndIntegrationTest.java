package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wires the explosion and the bomber spawn like the game does: both listeners run inside a real {@link PlayingPhase}
 * and the round end is checked by {@link RoundConditions}.
 */
class BomberExplodeRoundEndIntegrationTest extends TamiasPlayerTestBase {

    private static final Pos TNT_POSITION = new Pos(8, 40, 8);

    private TeamService teamService;
    private StaminaService staminaService;
    private Deque<Pos> tntPositions;
    private PlayingPhase playingPhase;

    @BeforeEach
    void setUp() {
        this.teamService = TeamService.of();
        TeamHelper.loadTeams(10, this.teamService);
        this.staminaService = new StaminaService();
        this.tntPositions = new ArrayDeque<>();
        this.tntPositions.add(TNT_POSITION);

        LinearPhaseSeries<Phase> phaseSeries = new LinearPhaseSeries<>("test");
        BooleanSupplier roundEndCheck = () -> RoundConditions.checkRoundEnd(phaseSeries, this.teamService);
        Map<Class<? extends Event>, Consumer<? extends Event>> listeners = Map.of(
                BomberExplodeEvent.class, new BomberExplodeListener(this.teamService, roundEndCheck),
                RoleToBomberChangeEvent.class, new RoleToBomberChangeListener(
                        this.teamService, this.staminaService, this.tntPositions::poll, roundEndCheck::getAsBoolean
                )
        );
        this.playingPhase = new PlayingPhase(300, _ -> {}, () -> {}, () -> listeners);
        phaseSeries.add(this.playingPhase);
        phaseSeries.add(new EndPhase());
        phaseSeries.start();
    }

    @AfterEach
    void tearDown() {
        this.staminaService.cleanUp();
    }

    @Test
    void testLastSurvivorKeepsTheTntWhenTheRoundEnds(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player lastSurvivor = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);

        EventDispatcher.call(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertTrue(this.playingPhase.isFinished());
        assertEquals(GameConfig.BOMBER_KEY.asString(), lastSurvivor.getTag(Tags.TEAM_KEY));
        assertEquals(1, this.tntPositions.size());
        assertEquals(Block.TNT, instance.getBlock(TNT_POSITION));
        assertEquals(new Pos(2, 40, 0), lastSurvivor.getPosition());
        assertFalse(SpectatorHelper.isSpectator(lastSurvivor));

        env.destroyInstance(instance, true);
    }

    @Test
    void testBlownUpSurvivorSpawnsOnTntWhileTheRoundGoesOn(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player blownUp = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);
        createPlayer(env, instance, new Pos(20, 40, 20), GameConfig.SURVIVOR_KEY);

        EventDispatcher.call(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertFalse(this.playingPhase.isFinished());
        assertEquals(GameConfig.BOMBER_KEY.asString(), blownUp.getTag(Tags.TEAM_KEY));
        assertTrue(this.tntPositions.isEmpty());
        assertEquals(Block.AIR, instance.getBlock(TNT_POSITION));
        assertEquals(TNT_POSITION.add(0.5, 0, 0.5), blownUp.getPosition().withView(0f, 0f));
        assertFalse(SpectatorHelper.isSpectator(blownUp));

        env.destroyInstance(instance, true);
    }

    @Test
    void testAllBlownUpSurvivorsKeepTheTntWhenTheRoundEnds(@NotNull Env env) {
        Instance instance = createInstance(env);
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        Player first = createPlayer(env, instance, new Pos(2, 40, 0), GameConfig.SURVIVOR_KEY);
        Player second = createPlayer(env, instance, new Pos(0, 40, 2), GameConfig.SURVIVOR_KEY);

        EventDispatcher.call(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertTrue(this.playingPhase.isFinished());
        assertEquals(1, this.tntPositions.size());
        assertEquals(Block.TNT, instance.getBlock(TNT_POSITION));
        assertFalse(SpectatorHelper.isSpectator(first));
        assertFalse(SpectatorHelper.isSpectator(second));

        env.destroyInstance(instance, true);
    }

    private Instance createInstance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.setExplosionSupplier(new ExplosionCreator());
        instance.loadChunk(TNT_POSITION).join();
        instance.setBlock(TNT_POSITION, Block.TNT);
        return instance;
    }

    private Player createPlayer(Env env, Instance instance, Pos pos, Key teamKey) {
        Player player = env.createPlayer(instance, pos);
        TeamHelper.addPlayerToTeam(TeamHelper.getTeam(this.teamService, teamKey), player);
        return player;
    }

    private static final class EndPhase extends Phase {

        private EndPhase() {
            super("end");
        }

        @Override
        protected void onStart() {
            // Only marks the end of the playing phase
        }
    }
}
