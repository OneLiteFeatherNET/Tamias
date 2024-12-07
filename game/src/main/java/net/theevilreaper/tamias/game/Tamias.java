package net.theevilreaper.tamias.game;

import de.icevizion.aves.map.MapEntry;
import de.icevizion.aves.util.functional.PlayerConsumer;
import de.icevizion.aves.util.functional.VoidConsumer;
import de.icevizion.xerus.api.ColorData;
import de.icevizion.xerus.api.phase.CyclicPhaseSeries;
import de.icevizion.xerus.api.phase.LinearPhaseSeries;
import de.icevizion.xerus.api.phase.Phase;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamCreator;
import de.icevizion.xerus.api.team.TeamService;
import de.icevizion.xerus.api.team.TeamServiceImpl;
import de.icevizion.xerus.api.team.event.MultiPlayerTeamEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.InstanceContainer;
import net.theevilreaper.tamias.common.ListenerHandling;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.config.GameConfigReader;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.common.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.commands.StartCommand;
import net.theevilreaper.tamias.game.commands.TestCommand;
import net.theevilreaper.tamias.game.event.BomberRequireSpawnEvent;
import net.theevilreaper.tamias.game.listener.PlayerChatListener;
import net.theevilreaper.tamias.game.listener.PlayerJoinListener;
import net.theevilreaper.tamias.game.listener.PlayerQuitListener;
import net.theevilreaper.tamias.game.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.game.listener.game.BomberReviveListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileBlockListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileEntityListener;
import net.theevilreaper.tamias.game.listener.game.RoundFinishListener;
import net.theevilreaper.tamias.game.listener.team.TeamActionListener;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.MapBuildPhase;
import net.theevilreaper.tamias.game.phase.PlayingPhase;
import net.theevilreaper.tamias.game.phase.PrePlayingPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TamiasTeamCreator;
import net.theevilreaper.tamias.game.util.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class Tamias extends Extension implements ListenerHandling {

    public static final Logger LOGGER = LoggerFactory.getLogger(Tamias.class);
    private final LinearPhaseSeries<Phase> phaseSeries;
    private final TeamService<Team> teamService;
    private final StaminaService staminaService;
    private final GameConfig gameConfig;
    private final Items items;

    private InstanceContainer instance;
    private IntConsumer timeUpdater;
    private TamiasScoreboard scoreboard;

    private MapProvider mapProvider;

    public Tamias() {
        this.gameConfig = new GameConfigReader(Paths.get("")).getConfig();
        this.phaseSeries = new LinearPhaseSeries<>();
        this.teamService = new TeamServiceImpl<>();
        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        this.initTeams();
        this.staminaService = new StaminaService();
        this.items = new Items();
    }

    @Override
    public void initialize() {
        checkMapDirectory();
        instance.enableAutoChunkLoad(true);
        registerCancelListener(MinecraftServer.getGlobalEventHandler());
        MinecraftServer.getInstanceManager().registerInstance(instance);

        MinecraftServer.getCommandManager().register(new StartCommand(this.phaseSeries::getCurrentPhase));

        this.mapProvider = new MapProvider(Paths.get("").resolve("maps"), pathStream -> pathStream.map(MapEntry::of).toList());
        this.mapProvider.loadLobbyMap(this.instance);

        this.scoreboard = TamiasScoreboard.of(3);
        this.scoreboard.initDefaults();
        this.timeUpdater = this.scoreboard::updateTime;

        this.createPhaseStructure();
        registerListener(MinecraftServer.getGlobalEventHandler());
        MinecraftServer.getCommandManager().register(new TestCommand());
        this.phaseSeries.start();
        this.scoreboard.updateMapName("Test");
    }

    @Override
    public void terminate() {

    }

    private void createPhaseStructure() {
        this.phaseSeries.add(new LobbyPhase(
                this.mapProvider,
                gameConfig.minPlayers(),
                gameConfig.maxPlayers(),
                gameConfig.lobbyTime(),
                timeUpdater
        ));
        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        gameSeries.add(new MapBuildPhase(this.mapProvider, this.mapProvider::getGameArea, this.scoreboard::removeViewer));

        VoidConsumer gamePreparation = () -> {
            int survivorCount = this.teamService.getTeams().get(GameConfig.SURVIVOR_ID).getPlayers().size();
            this.scoreboard.switchBoard(TamiasScoreboard.BoardType.GAME);
            this.scoreboard.updateGameDefaults(10, survivorCount, 1);
        };

        gameSeries.add(new PrePlayingPhase(
                this.teamService,
                this.mapProvider.getActiveMap(),
                gamePreparation,
                this.items
        ));
        gameSeries.add(new PlayingPhase(timeUpdater, () -> this.mapProvider.getSpawnArea()::reset, this.scoreboard::addViewer));
        gameSeries.setMaxIterations(this.gameConfig.maxRounds());
        this.phaseSeries.add(gameSeries);
        this.phaseSeries.add(new RestartPhase());
    }

    private void checkMapDirectory() {
        var dataDirectory = getDataDirectory();
        var mapDirectory = dataDirectory.resolve(GameConfig.MAP_FOLDER);
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectory(getDataDirectory());
            } catch (IOException exception) {
                LOGGER.error("Unable to create extension directory", exception);
            }
        }

        if (!Files.exists(mapDirectory)) {
            try {
                Files.createDirectory(mapDirectory);
            } catch (IOException exception) {
                LOGGER.error("Unable to create the map folder", exception);
            }
        }
    }

    private void initTeams() {
        TeamCreator teamCreator = new TamiasTeamCreator();
        int teamSize = this.gameConfig.teamSize();
        this.teamService.add(Team.builder(teamCreator)
                .colorData(ColorData.GREEN).name(GameConfig.SURVIVOR_TEAM_NAME).capacity(teamSize)
                .build()
        );
        this.teamService.add(Team.builder(teamCreator)
                .colorData(ColorData.RED).name(GameConfig.BOMBER_TEAM).capacity(teamSize)
                .build()
        );
    }

    public void registerGameListener(@NotNull EventNode<Event> eventNode) {
        Supplier<List<Team>> teamSupplier = this.teamService::getTeams;
        eventNode.addListener(RoundEndEvent.class, new RoundFinishListener(teamSupplier));

        GameArea gameArea = this.mapProvider.getGameArea();

        eventNode.addListener(BomberRequireSpawnEvent.class,
                new BomberReviveListener(this.staminaService::getStaminaBar, gameArea::getRandomPosition, items::setBombItem)
        );
    }

    void registerListener(@NotNull EventNode<Event> node) {
        Supplier<Integer> supplier = this.gameConfig::maxPlayers;
        PlayerConsumer playerConsumer = player -> player.teleport(this.mapProvider.getActiveMap().get().getSpawn());
        node.addListener(AsyncPlayerConfigurationEvent.class, new PlayerJoinListener(supplier, this.phaseSeries::getCurrentPhase, () -> this.instance));
        IntConsumer playerConsumerFunction = this.scoreboard::updatePlayerCount;
        node.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(this.phaseSeries::getCurrentPhase, playerConsumer, this.scoreboard::addViewer, playerConsumerFunction));
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(this.phaseSeries::getCurrentPhase, this.teamService.getTeams()::get, this::checkRoundEnd, playerConsumerFunction));
        node.addListener(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        node.addListener(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(player -> {
        }, this.staminaService::getStaminaBar));
        node.addListener(PlayerChatEvent.class, new PlayerChatListener());
        node.addListener((Class<MultiPlayerTeamEvent<Team>>) (Class<?>) MultiPlayerTeamEvent.class, new TeamActionListener());
    }

    void checkRoundEnd() {
        Phase currentPhase = this.phaseSeries.getCurrentPhase();
        if (!(currentPhase instanceof PlayingPhase)) return;

        Team survivorTeam = this.teamService.getTeams().getFirst();
        Team bomberTeam = this.teamService.getTeams().getLast();

        if (survivorTeam.isEmpty() || survivorTeam.getPlayers().size() - 1 == 0) {
            currentPhase.finish();
            return;
        }

        if (!bomberTeam.isEmpty()) return;
        currentPhase.finish();
        //TODO: Print winner

    }
}
