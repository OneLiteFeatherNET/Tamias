package net.theevilreaper.tamias.game.listener.game;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.TestConnection;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.explosion.ExplosionCreator;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.listener.round.RoundEndListener;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.player.TamiasPlayerTestBase;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.team.component.StaminaComponent;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wires the explosion, the bomber spawn and the round end like the game does: the listeners run inside a real
 * {@link PlayingPhase}, the round end is checked by {@link RoundConditions} and handled by the {@link RoundEndListener}.
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
                ),
                RoundEndEvent.class, new RoundEndListener(this.teamService::getTeams, this.staminaService::cleanUp, (_, _) -> {})
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
        TestConnection survivorConnection = env.createConnection();
        Player lastSurvivor = survivorConnection.connect(instance, new Pos(2, 40, 0));
        addToTeam(lastSurvivor, GameConfig.SURVIVOR_KEY);
        Collector<SystemChatPacket> messages = survivorConnection.trackIncoming(SystemChatPacket.class);

        EventDispatcher.call(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertTrue(this.playingPhase.isFinished());
        assertEquals(1, this.tntPositions.size());
        assertEquals(Block.TNT, instance.getBlock(TNT_POSITION));
        assertEquals(new Pos(2, 40, 0), lastSurvivor.getPosition());

        // The round end resets the players and removes every stamina bar
        assertFalse(SpectatorHelper.isSpectator(lastSurvivor));
        assertNull(lastSurvivor.getTag(Tags.TEAM_KEY));
        assertNull(this.staminaService.getStaminaBar(lastSurvivor));
        assertNull(this.staminaService.getStaminaBar(bomber));

        // Plain text, because the round end already removed the team colors from the names
        List<String> chat = messages.collect().stream().map(packet -> plainText(packet.message())).toList();
        assertFalse(chat.contains(plainText(GameMessages.getSpectatorMessage(lastSurvivor))));
        assertTrue(chat.contains(plainText(GameMessages.getDeathMessage(lastSurvivor, bomber))));

        env.destroyInstance(instance, true);
    }

    @Test
    void testLastSurvivorGetsNoSpectatorMessageWithoutFreeTnt(@NotNull Env env) {
        Instance instance = createInstance(env);
        this.tntPositions.clear();
        Player bomber = createPlayer(env, instance, new Pos(0, 40, 0), GameConfig.BOMBER_KEY);
        TestConnection survivorConnection = env.createConnection();
        Player lastSurvivor = survivorConnection.connect(instance, new Pos(2, 40, 0));
        addToTeam(lastSurvivor, GameConfig.SURVIVOR_KEY);
        Collector<SystemChatPacket> messages = survivorConnection.trackIncoming(SystemChatPacket.class);

        EventDispatcher.call(new BomberExplodeEvent(bomber, bomber.getPosition().asVec()));

        assertTrue(this.playingPhase.isFinished());
        assertFalse(SpectatorHelper.isSpectator(lastSurvivor));
        assertNull(this.staminaService.getStaminaBar(lastSurvivor));

        List<String> chat = messages.collect().stream().map(packet -> plainText(packet.message())).toList();
        assertFalse(chat.contains(plainText(GameMessages.getSpectatorMessage(lastSurvivor))));
        assertTrue(chat.contains(plainText(GameMessages.getDeathMessage(lastSurvivor, bomber))));

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
        assertNull(this.staminaService.getStaminaBar(first));
        assertNull(this.staminaService.getStaminaBar(second));

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
        addToTeam(player, teamKey);
        return player;
    }

    /**
     * Adds the player to the team and gives them the stamina bar of the team, like the start of a round does.
     */
    private void addToTeam(Player player, Key teamKey) {
        Team team = TeamHelper.getTeam(this.teamService, teamKey);
        TeamHelper.addPlayerToTeam(team, player);
        StaminaComponent staminaComponent = team.get(StaminaComponent.class);
        if (staminaComponent != null) {
            this.staminaService.add(player.getUuid(), staminaComponent.staminaFactory().apply(player));
        }
    }

    private static String plainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
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
