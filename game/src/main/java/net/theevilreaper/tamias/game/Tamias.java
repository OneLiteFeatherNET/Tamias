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
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.validate.Check;
import net.theevilreaper.tamias.common.ListenerHandling;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.config.GameConfigReader;
import net.theevilreaper.tamias.common.map.MapProvider;
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
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.MapBuildPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PostPlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PrePlayingPhase;
import net.theevilreaper.tamias.game.round.RoundProvider;
import net.theevilreaper.tamias.game.scoreboard.TamiasScoreboard;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.tamias.game.stamina.StaminaFactory;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TamiasTeamCreator;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.FileChecker;
import net.theevilreaper.tamias.game.util.Items;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class Tamias extends Extension implements ListenerHandling {

    private final LinearPhaseSeries<Phase> phaseSeries;
    private final TeamService<Team> teamService;
    private final StaminaService staminaService;
    private final GameConfig gameConfig;
    private final Items items;
    private final RoundProvider roundProvider;
    private final IntConsumer timeUpdater;
    private final TamiasScoreboard scoreboard;

    private MapProvider mapProvider;

    public Tamias() {
        Path path = Paths.get("");
        this.gameConfig = new GameConfigReader(path).getConfig();
        this.phaseSeries = new LinearPhaseSeries<>();
        this.teamService = new TeamServiceImpl<>();
        this.mapProvider = new MapProvider(path.resolve("maps"), pathStream -> pathStream.map(MapEntry::of).toList());
        this.initTeams();
        this.staminaService = new StaminaService();
        this.items = new Items();
        this.roundProvider = new RoundProvider(this.gameConfig.maxRounds());
        this.scoreboard = TamiasScoreboard.of(this.gameConfig.maxRounds());
        this.timeUpdater = this.scoreboard::updateTime;
    }

    @Override
    public void initialize() {
        FileChecker.checkFileIntegrity(getDataDirectory());
        registerCancelListener(MinecraftServer.getGlobalEventHandler());
        InstanceContainer instance = this.mapProvider.getActiveInstance().get();
        MinecraftServer.getInstanceManager().registerInstance(instance);

        MinecraftServer.getCommandManager().register(new StartCommand(this.phaseSeries::getCurrentPhase));

        this.mapProvider = new MapProvider(Paths.get("").resolve("maps"), pathStream -> pathStream.map(MapEntry::of).toList());
        this.mapProvider.loadLobbyMap();

        this.scoreboard.initDefaults();

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
                timeUpdater,
                this.roundProvider::triggerNextRound
        ));
        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        Consumer<List<Player>> teleportConsumer = players -> this.mapProvider.teleportPlayers(players, this.roundProvider::isFirstRound);
        gameSeries.add(new MapBuildPhase(teleportConsumer, this.mapProvider::getGameArea, this.scoreboard::removeViewer));

        VoidConsumer gamePreparation = () -> {
            int survivorCount = this.teamService.getTeams().get(GameConfig.SURVIVOR_ID).getPlayers().size();
            this.scoreboard.switchBoard(TamiasScoreboard.BoardType.GAME);
            this.scoreboard.updateGameDefaults(10, survivorCount, 1);
        };

        VoidConsumer staminaCreation = () -> {
            Team survivorTeam = this.teamService.getTeams().get(GameConfig.SURVIVOR_ID);
            Team bomberTeam = this.teamService.getTeams().get(GameConfig.TNT_ID);

            Map<UUID, StaminaBar> staminaBars = new HashMap<>();

            for (Player player : survivorTeam.getPlayers()) {
                staminaBars.put(player.getUuid(), StaminaFactory.createShootBar(player));
            }

            for (Player player : bomberTeam.getPlayers()) {
                staminaBars.put(player.getUuid(), StaminaFactory.createExplodeBar(player));
            }
            this.staminaService.add(staminaBars);
        };

        gameSeries.add(new PrePlayingPhase(
                this.teamService,
                this.mapProvider.getActiveMap(),
                gamePreparation,
                this.items::setItemToPlayer,
                staminaCreation
        ));

        VoidConsumer startLogic = () -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                AttributeHelper.enableMovement(player);
                this.scoreboard.addViewer(player);
            }
            this.mapProvider.getSpawnArea().reset();
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
                        () -> this.mapProvider.getGameArea()::reset,
                        () -> this.mapProvider.getSpawnArea()::triggerPlacement,
                        teleportConsumer
                )
        );
        gameSeries.setMaxIterations(this.gameConfig.maxRounds());
        this.phaseSeries.add(gameSeries);
        this.phaseSeries.add(new RestartPhase());
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

    public @NotNull Map<Class<? extends Event>, Consumer<? extends Event>> registerGameListener() {
        Map<Class<? extends Event>, Consumer<? extends Event>> listenerMap = new HashMap<>();
        listenerMap.put(RoundEndEvent.class, new RoundFinishListener(this.teamService::getTeams));

        Optional<Supplier<Pos>> randomPos = Optional.ofNullable(() -> this.mapProvider.getGameArea().getRandomPosition());
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
        node.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(this.phaseSeries::getCurrentPhase, this.mapProvider::teleportToActiveSpawn, this.scoreboard::addViewer, playerConsumerFunction));
        node.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(this.phaseSeries::getCurrentPhase, this.teamService.getTeams()::get, this::checkRoundEnd, playerConsumerFunction));
        node.addListener(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        node.addListener(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(player -> {
        }, this.staminaService::getStaminaBar));
        node.addListener(PlayerChatEvent.class, new PlayerChatListener());
        node.addListener((Class<MultiPlayerTeamEvent<Team>>) (Class<?>) MultiPlayerTeamEvent.class, new TeamActionListener());

        // Listener for rounds
        node.addListener(RoundStartEvent.class, new RoundStartListener(this.scoreboard::updateRound));
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
