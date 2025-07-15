package net.theevilreaper.tamias.game;

import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.CyclicPhaseSeries;
import net.theevilreaper.xerus.api.phase.LinearPhaseSeries;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.TeamService;
import net.theevilreaper.xerus.api.team.TeamServiceImpl;
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
import net.theevilreaper.tamias.common.round.event.RoundEndEvent;
import net.theevilreaper.tamias.common.round.event.RoundStartEvent;
import net.theevilreaper.tamias.game.attribute.AttributeHelper;
import net.theevilreaper.tamias.game.commands.StartCommand;
import net.theevilreaper.tamias.game.commands.TestCommand;
import net.theevilreaper.tamias.game.event.BomberExplodeEvent;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.listener.PlayerChatListener;
import net.theevilreaper.tamias.game.listener.PlayerJoinListener;
import net.theevilreaper.tamias.game.listener.PlayerQuitListener;
import net.theevilreaper.tamias.game.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.game.listener.game.BomberExplodeListener;
import net.theevilreaper.tamias.game.listener.game.BomberReviveListener;
import net.theevilreaper.tamias.game.listener.game.PlayerInteractItemListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileBlockListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileEntityListener;
import net.theevilreaper.tamias.game.listener.round.RoundFinishListener;
import net.theevilreaper.tamias.game.listener.round.RoundStartListener;
import net.theevilreaper.tamias.game.listener.team.TeamActionListener;
import net.theevilreaper.tamias.game.map.GameMapProvider;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.MapBuildPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PostPlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PrePlayingPhase;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.tamias.game.round.RoundProvider;
import net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.FileChecker;
import net.theevilreaper.tamias.game.util.Items;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
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
    private final TeamService<Team> teamService;
    private final StaminaService staminaService;
    private final GameConfig gameConfig;
    private final Items items;
    private final RoundProvider roundProvider;
    private final IntConsumer timeUpdater;
    private final TamiasScoreboard scoreboard;
    private final MapProvider mapProvider;

    public Tamias() {
        Path path = Paths.get("");
        this.gameConfig = new GameConfigReader(path.resolve("config")).getConfig();
        this.phaseSeries = new LinearPhaseSeries<>();
        this.teamService = new TeamServiceImpl<>();
        this.mapProvider = new GameMapProvider(path);
        TeamHelper.loadTeams(this.gameConfig.teamSize(), this.teamService);
        this.staminaService = new StaminaService();
        this.items = new Items();
        this.roundProvider = new RoundProvider(this.gameConfig.maxRounds());
        this.scoreboard = TamiasScoreboard.create();
        this.timeUpdater = this.scoreboard::updateTime;
    }

    public void initialize() {
        FileChecker.checkFileIntegrity(Paths.get(""));
        registerCancelListener(MinecraftServer.getGlobalEventHandler());

        MinecraftServer.getCommandManager().register(new StartCommand(this.phaseSeries::getCurrentPhase));

        this.scoreboard.initDefaults();
        this.createPhaseStructure();
        registerListener(MinecraftServer.getGlobalEventHandler());
        MinecraftServer.getCommandManager().register(new TestCommand());
        this.phaseSeries.start();
        this.scoreboard.updateMapName("Test");
    }

    public void terminate() {

    }

    private void createPhaseStructure() {
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;

        this.phaseSeries.add(new LobbyPhase(
                this.mapProvider,
                timeUpdater,
                this.roundProvider::triggerNextRound,
                this.gameConfig.minPlayers(),
                this.gameConfig.maxPlayers(),
                this.gameConfig.lobbyTime()
        ));
        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        Consumer<List<Player>> teleportConsumer = players -> gameMapProvider.teleportPlayers(players, this.roundProvider::isFirstRound);
        gameSeries.add(new MapBuildPhase(
                gameMapProvider::switchInstanceHolding,
                teleportConsumer,
                () -> {
                    return null;
                }
        ));

        VoidConsumer gamePreparation = () -> {
            int survivorCount = this.teamService.getTeams().get(GameConfig.SURVIVOR_ID).getPlayers().size();
            this.scoreboard.switchBoard(TamiasScoreboard.BoardType.GAME);
            this.scoreboard.updateGameDefaults(10, survivorCount, 1);
        };

        gameSeries.add(new PrePlayingPhase(
                this.teamService,
                gameMapProvider::getActiveMap,
                gamePreparation,
                this.items::setItemToPlayer,
                () -> staminaService.createStaminaObjects(this.teamService)
        ));

        VoidConsumer startLogic = () -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                AttributeHelper.enableMovement(player);
                this.scoreboard.addViewer(player);
            }
            gameMapProvider.resetSpawnArea();
            this.staminaService.start();
        };

        gameSeries.add(
                new PlayingPhase(
                        timeUpdater,
                        startLogic,
                        this::registerGameListener
                )
        );

        gameSeries.add(
                new PostPlayingPhase(
                        this.roundProvider::isLastRound,
                        this.roundProvider::triggerNextRound,
                        this.scoreboard::hideBoard,
                        teleportConsumer
                )
        );
        gameSeries.setMaxIterations(this.gameConfig.maxRounds());
        this.phaseSeries.add(gameSeries);
        this.phaseSeries.add(new RestartPhase());
    }

    public @NotNull Map<Class<? extends Event>, Consumer<? extends Event>> registerGameListener() {
        Map<Class<? extends Event>, Consumer<? extends Event>> listenerMap = new HashMap<>();
        listenerMap.put(RoundEndEvent.class, new RoundFinishListener(this.teamService::getTeams));
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;
        Supplier<Pos> randomPos = gameMapProvider.getGameArea()::getRandomPosition;
        listenerMap.put(PlayerUseItemEvent.class, new PlayerInteractItemListener(staminaService::getStaminaBar));
        listenerMap.put(BomberRequireSpawnEvent.class, new BomberReviveListener(this.staminaService::getStaminaBar, randomPos, items::setBombItem));
        listenerMap.put(BomberExplodeEvent.class, new BomberExplodeListener());
        listenerMap.put(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        PlayerConsumer teamUpdater = player -> TeamHelper.switchToTNTTeam(this.teamService.getTeams()::get, player);
        listenerMap.put(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(teamUpdater, staminaService::getStaminaBar));
        return listenerMap;
    }

    void registerListener(@NotNull EventNode<Event> node) {
        node.addListener(AsyncPlayerConfigurationEvent.class,
                new PlayerJoinListener(this.phaseSeries::getCurrentPhase, this.mapProvider.getActiveInstance(), gameConfig.maxPlayers())
        );
        IntConsumer playerConsumerFunction = this.scoreboard::updatePlayerCount;
        PlayerConsumer teleportConsumer = player -> this.mapProvider.teleportToSpawn(player, false);
        node.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(this.phaseSeries::getCurrentPhase, teleportConsumer, this.scoreboard::addViewer, playerConsumerFunction));
        VoidConsumer checkRoundEnd = () -> RoundConditions.checkRoundEnd(this.phaseSeries, this.teamService);
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(this.phaseSeries::getCurrentPhase, this.teamService.getTeams()::get, checkRoundEnd, playerConsumerFunction));
        node.addListener(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        node.addListener(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(player -> {
        }, this.staminaService::getStaminaBar));
        node.addListener(PlayerChatEvent.class, new PlayerChatListener());
        node.addListener((Class<MultiPlayerTeamEvent<Team>>) (Class<?>) MultiPlayerTeamEvent.class, new TeamActionListener());

        // Listener for rounds
        node.addListener(RoundStartEvent.class, new RoundStartListener(this.scoreboard::updateRound));
    }
}
