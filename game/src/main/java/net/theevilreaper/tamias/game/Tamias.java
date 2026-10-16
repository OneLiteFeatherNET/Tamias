package net.theevilreaper.tamias.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.aves.util.Strings;
import net.theevilreaper.aves.util.TimeFormat;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.bootstrap.ServiceBootstrap;
import net.theevilreaper.tamias.game.scoreboard.GameScoreboard;
import net.theevilreaper.tamias.game.scoreboard.LobbyScoreboard;
import net.theevilreaper.tamias.game.scoreboard.ScoreType;
import net.theevilreaper.tamias.game.scoreboard.Scoreboard;
import net.theevilreaper.tamias.game.stamina.event.StaminaCreateEvent;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.round.event.RoundStartEvent;
import net.theevilreaper.tamias.game.util.phase.LobbyPhaseData;
import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.TeamService;
import net.theevilreaper.xerus.api.team.event.MultiPlayerTeamEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.theevilreaper.tamias.common.ListenerHandling;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.config.GameConfigReader;
import net.theevilreaper.tamias.common.event.AreaSpawnTriggerEvent;
import net.theevilreaper.tamias.common.event.GameAreaChunksReadyEvent;
import net.theevilreaper.tamias.common.event.SpawnCleanupEvent;
import net.theevilreaper.tamias.common.event.GameCleanupEvent;
import net.theevilreaper.tamias.common.ground.GroundData;
import net.theevilreaper.tamias.common.ground.GroundDataRegistry;
import net.theevilreaper.tamias.common.map.event.MapPrepareEvent;
import net.theevilreaper.tamias.game.round.event.RoundPrepareEvent;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.commands.StartCommand;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.event.RoleToBomberChangeEvent;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.listener.PlayerChatListener;
import net.theevilreaper.tamias.game.listener.PlayerJoinListener;
import net.theevilreaper.tamias.game.listener.PlayerQuitListener;
import net.theevilreaper.tamias.game.util.phase.PhaseSeriesHelper;
import net.theevilreaper.tamias.game.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.game.listener.area.AreaSpawnListener;
import net.theevilreaper.tamias.game.listener.area.GameAreaPrepareListener;
import net.theevilreaper.tamias.game.listener.area.SpawnCleanupListener;
import net.theevilreaper.tamias.game.listener.area.GameCleanupListener;
import net.theevilreaper.tamias.game.listener.area.MapPrepareListener;
import net.theevilreaper.tamias.game.listener.game.BomberExplodeListener;
import net.theevilreaper.tamias.game.listener.game.BomberReviveListener;
import net.theevilreaper.tamias.game.listener.game.PlayerInteractItemListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileBlockListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileEntityListener;
import net.theevilreaper.tamias.game.listener.game.RoleToBomberChangeListener;
import net.theevilreaper.tamias.game.listener.round.RoundEndListener;
import net.theevilreaper.tamias.game.listener.round.RoundPrepareListener;
import net.theevilreaper.tamias.game.listener.round.RoundStartListener;
import net.theevilreaper.tamias.game.listener.team.TeamActionListener;
import net.theevilreaper.tamias.game.map.GameMapProvider;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.GroundBuildPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PostPlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PrePlayingPhase;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.round.GameResultAnnouncer;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.tamias.game.round.RoundProvider;
import net.theevilreaper.tamias.game.round.RoundWins;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.FileChecker;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class Tamias implements ListenerHandling {

    private final LinearPhaseSeries<Phase> phaseSeries;
    private final TeamService teamService;
    private final StaminaService staminaService;
    private final GameConfig gameConfig;
    private final IntConsumer timeUpdater;
    private final LobbyScoreboard lobbyScoreboard;
    private final GameScoreboard gameScoreboard;
    private final MapProvider mapProvider;

    private volatile Scoreboard activeScoreboard;
    private RoundProvider roundProvider;
    private CyclicPhaseSeries<Phase> gameSeries;
    private int playedRounds;

    public Tamias() {
        Path path = ServiceBootstrap.resolveWorkingDirectory();
        this.gameConfig = new GameConfigReader(path.resolve("config")).getConfig();
        this.phaseSeries = new LinearPhaseSeries<>("game");
        this.teamService = TeamService.of();
        this.mapProvider = new GameMapProvider(path, gameConfig.maxPlayers());
        TeamHelper.loadTeams(this.gameConfig.teamSize(), this.teamService);
        this.staminaService = new StaminaService();
        this.lobbyScoreboard = new LobbyScoreboard(GameMessages.getTitleTime(this.gameConfig.lobbyTime()));
        this.gameScoreboard = new GameScoreboard();
        this.activeScoreboard = this.lobbyScoreboard;
        this.timeUpdater = value -> {
            Component time = Component.text("Time:", NamedTextColor.GOLD).append(Component.space())
                    .append(Component.text(Strings.getTimeString(TimeFormat.MM_SS, value), NamedTextColor.YELLOW));
            this.activeScoreboard.updateTitle(time);
        };
    }

    public void initialize() {
        FileChecker.checkFileIntegrity(Paths.get(""));
        registerCancelListener(MinecraftServer.getGlobalEventHandler());

        MinecraftServer.getCommandManager().register(new StartCommand(this.phaseSeries::getCurrentPhase));

        this.lobbyScoreboard.initDefaults();
        this.gameScoreboard.initDefaults();
        this.createPhaseStructure();
        registerListener(MinecraftServer.getGlobalEventHandler());
        this.phaseSeries.start();
        this.lobbyScoreboard.updateScore(ScoreType.MAP_TYPE, Component.text("Test"));
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::terminate);
    }

    public void terminate() {
        closeMapProvider(this.mapProvider);
    }

    /**
     * Resets the online players' exp bar/level back to their default state.
     * Called once ground building finishes; {@link net.theevilreaper.tamias.common.area.placement.AreaBasePlacement}
     * uses the exp bar as a build-progress indicator while placement is running.
     */
    private void resetPlayerBuildProgress() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.setLevel(0);
            player.setExp(0f);
        }
    }

    private void createPhaseStructure() {
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;

        this.phaseSeries.add(new LobbyPhase(new LobbyPhaseData(this.timeUpdater, this.gameConfig)));

        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        this.gameSeries = gameSeries;
        this.roundProvider = new RoundProvider(gameSeries);
        gameSeries.add(new GroundBuildPhase(() -> {
            GroundData randomData = GroundDataRegistry.instance().getRandomData();
            gameMapProvider.getGamePlacement().triggerPlacement(randomData);
            return this::resetPlayerBuildProgress;
        }, gameMapProvider.getGamePlacement()));

        gameSeries.add(new PrePlayingPhase(this.teamService));

        VoidConsumer startLogic = () -> {
            RoundWins.snapshotRoles(
                    TeamHelper.getTeam(this.teamService, GameConfig.SURVIVOR_KEY),
                    TeamHelper.getTeam(this.teamService, GameConfig.BOMBER_KEY)
            );
            this.refreshSharedScores();
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                AttributeHelper.enableMovement(player);
                this.gameScoreboard.addViewer(player);
            }
            this.refreshWins();
            this.staminaService.start();
        };

        gameSeries.add(
                new PlayingPhase(
                        timeUpdater,
                        startLogic,
                        this::registerGameListener
                )
        );

        gameSeries.add(new PostPlayingPhase());
        gameSeries.setMaxIterations(this.gameConfig.maxRounds());
        this.phaseSeries.add(gameSeries);
        this.phaseSeries.add(new RestartPhase(() ->
                GameResultAnnouncer.announce(this.playedRounds, MinecraftServer.getConnectionManager().getOnlinePlayers())
        ));
    }

    public @NotNull Map<Class<? extends Event>, Consumer<? extends Event>> registerGameListener() {
        Map<Class<? extends Event>, Consumer<? extends Event>> listenerMap = new HashMap<>();
        listenerMap.put(RoundEndEvent.class, new RoundEndListener(this.teamService::getTeams, this.staminaService::cleanUp, (survivors, bombers) -> {
            RoundWins.credit(survivors, bombers);
            this.playedRounds++;
            this.refreshWins();
        }));
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;

        // Every dropped TNT is a respawn ticket for the bomber team, null means no TNT is left
        Supplier<Pos> randomPos = () -> {
            Pos position = gameMapProvider.getGameArea().getRandomPosition();
            this.refreshSharedScores();
            return position;
        };
        listenerMap.put(PlayerUseItemEvent.class, new PlayerInteractItemListener(staminaService::getStaminaBar));
        listenerMap.put(BomberRequireSpawnEvent.class, new BomberReviveListener(
                this.staminaService::getStaminaBar,
                randomPos,
                player -> this.staminaService.removeStaminaBar(player.getUuid()),
                this::checkRoundEnd
        ));
        listenerMap.put(BomberExplodeEvent.class, new BomberExplodeListener(
                player -> TeamHelper.switchToTNTTeam(this.teamService, player),
                this::checkRoundEnd
        ));
        listenerMap.put(RoleToBomberChangeEvent.class, new RoleToBomberChangeListener(
                this.teamService,
                this.staminaService,
                randomPos,
                this::checkRoundEnd
        ));
        listenerMap.put(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        listenerMap.put(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(staminaService::getStaminaBar));
        return listenerMap;
    }

    /**
     * Hides every scoreboard while the map is built. The game scoreboard is shown again when the round is played.
     */
    private void hideScoreboards() {
        if (this.activeScoreboard == this.lobbyScoreboard) {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                this.lobbyScoreboard.removeViewer(player);
            }
            this.activeScoreboard = this.gameScoreboard;
        }
        this.gameScoreboard.removeAllViewers();
    }

    /**
     * Updates the remaining TNT tickets and survivors on the game scoreboard.
     */
    private void refreshSharedScores() {
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;
        int tntTickets = gameMapProvider.getGameArea().getTntPositions().size();
        int survivors = this.teamService.getTeam(GameConfig.SURVIVOR_KEY).map(team -> team.getPlayers().size()).orElse(0);
        this.gameScoreboard.updateScore(ScoreType.TNT, Component.text(tntTickets));
        this.gameScoreboard.updateScore(ScoreType.PLAYER, Component.text(survivors, NamedTextColor.GRAY));
    }

    /**
     * Updates the round wins of every online player on the game scoreboard.
     */
    private void refreshWins() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(player instanceof TamiasPlayer tamiasPlayer)) continue;
            this.gameScoreboard.updateScore(player, ScoreType.WINS, this.roundProvider.getWinsComponent(tamiasPlayer.getRoundWins()));
        }
    }

    /**
     * Refreshes the shared scores, because a survivor or a TNT ticket may be gone, and checks if the round is over.
     */
    private void checkRoundEnd() {
        this.refreshSharedScores();
        RoundConditions.checkRoundEnd(this.phaseSeries, this.teamService);
    }

    void registerListener(@NotNull EventNode<Event> node) {
        node.addListener(AsyncPlayerConfigurationEvent.class,
                new PlayerJoinListener(this.phaseSeries::getCurrentPhase, this.mapProvider.getActiveInstance(), gameConfig.maxPlayers())
        );
        PlayerConsumer teleportConsumer = player -> this.mapProvider.teleportToSpawn(player, false);
        node.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(this.phaseSeries::getCurrentPhase, teleportConsumer, player -> this.activeScoreboard.addViewer(player)));
        node.addListener(PlayerDisconnectEvent.class,
                new PlayerQuitListener(
                        this.phaseSeries::getCurrentPhase,
                        key -> this.teamService.getTeam(key).orElse(null),
                        player -> this.staminaService.removeStaminaBar(player.getUuid()),
                        this::checkRoundEnd,
                        player -> this.activeScoreboard.removeViewer(player),
                        () -> PhaseSeriesHelper.finishEarly(this.gameSeries)
                )
        );
        node.addListener(PlayerChatEvent.class, new PlayerChatListener());
        node.addListener(MultiPlayerTeamEvent.class, new TeamActionListener());

        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;
        node.addListener(MapPrepareEvent.class, new MapPrepareListener(gameMapProvider.getGamePlacement()));
        node.addListener(AreaSpawnTriggerEvent.class, new AreaSpawnListener(gameMapProvider.getSpawnPlacement()));
        node.addListener(GameAreaChunksReadyEvent.class, new GameAreaPrepareListener(gameMapProvider.getGamePlacement()));
        node.addListener(SpawnCleanupEvent.class, new SpawnCleanupListener(gameMapProvider.getSpawnPlacement()));
        node.addListener(GameCleanupEvent.class, new GameCleanupListener(gameMapProvider.getGamePlacement()));
        node.addListener(RoundPrepareEvent.class, new RoundPrepareListener(gameMapProvider.getSpawnArea(), gameMapProvider.getActiveInstance().get()));

        // Listener for rounds
        node.addListener(RoundStartEvent.class, _ -> this.hideScoreboards());
        node.addListener(RoundStartEvent.class, new RoundStartListener(this.gameScoreboard, this.roundProvider));

        //Stamina listeners
        node.addListener(StaminaCreateEvent.class, _ ->
                this.staminaService.createStaminaObjects(this.teamService)
        );
    }
}
