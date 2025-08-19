package net.theevilreaper.tamias.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.theevilreaper.aves.map.provider.MapProvider;
import net.theevilreaper.aves.util.Strings;
import net.theevilreaper.aves.util.TimeFormat;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.game.scoreboard.LobbyScoreboard;
import net.theevilreaper.tamias.game.scoreboard.ScoreType;
import net.theevilreaper.tamias.game.scoreboard.Scoreboard;
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
import net.theevilreaper.tamias.game.listener.round.RoundEndListener;
import net.theevilreaper.tamias.game.listener.round.RoundStartListener;
import net.theevilreaper.tamias.game.listener.team.TeamActionListener;
import net.theevilreaper.tamias.game.map.GameMapProvider;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.GroundBuildPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PostPlayingPhase;
import net.theevilreaper.tamias.game.phase.playing.PrePlayingPhase;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.tamias.game.round.RoundProvider;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.FileChecker;
import net.theevilreaper.tamias.game.util.Items;
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
    private final Items items;
    private final IntConsumer timeUpdater;
    private final Scoreboard scoreboard;
    private final MapProvider mapProvider;

    private RoundProvider roundProvider;

    public Tamias() {
        Path path = Paths.get("");
        this.gameConfig = new GameConfigReader(path.resolve("config")).getConfig();
        this.phaseSeries = new LinearPhaseSeries<>("game");
        this.teamService = TeamService.of();
        this.mapProvider = new GameMapProvider(path);
        TeamHelper.loadTeams(this.gameConfig.teamSize(), this.teamService);
        this.staminaService = new StaminaService();
        this.items = new Items();
        this.scoreboard = new LobbyScoreboard(GameMessages.getTitleTime(this.gameConfig.lobbyTime()));
        this.timeUpdater = value -> {
            Component time = Component.text("Time:", NamedTextColor.GOLD).append(Component.space())
                    .append(Component.text(Strings.getTimeString(TimeFormat.MM_SS, value), NamedTextColor.YELLOW));
            this.scoreboard.updateTitle(time);
        };
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
        this.scoreboard.updateScore(ScoreType.MAP_TYPE, Component.text("Test"));
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::terminate);
    }

    public void terminate() {

    }

    private void createPhaseStructure() {
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;

        this.phaseSeries.add(new LobbyPhase(this.mapProvider, new LobbyPhaseData(this.timeUpdater, this.gameConfig)));

        CyclicPhaseSeries<Phase> gameSeries = new CyclicPhaseSeries<>("game");
        this.roundProvider = new RoundProvider(gameSeries);
        gameSeries.add(new GroundBuildPhase(
                () -> {
                    return null;
                }
        ));

        gameSeries.add(new PrePlayingPhase(
                this.teamService,
                this.items::setItemToPlayer,
                () -> staminaService.createStaminaObjects(this.teamService)
        ));

        VoidConsumer startLogic = () -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                AttributeHelper.enableMovement(player);
                this.scoreboard.addViewer(player);
            }
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
                        () -> {}
                )
        );
        gameSeries.setMaxIterations(this.gameConfig.maxRounds());
        this.phaseSeries.add(gameSeries);
        this.phaseSeries.add(new RestartPhase());
    }

    public @NotNull Map<Class<? extends Event>, Consumer<? extends Event>> registerGameListener() {
        Map<Class<? extends Event>, Consumer<? extends Event>> listenerMap = new HashMap<>();
        listenerMap.put(RoundEndEvent.class, new RoundEndListener(this.teamService::getTeams));
        GameMapProvider gameMapProvider = (GameMapProvider) this.mapProvider;

        Supplier<Pos> randomPos = () -> Pos.ZERO;//gameMapProvider.getGameArea()::getRandomPosition;
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
        PlayerConsumer teleportConsumer = player -> this.mapProvider.teleportToSpawn(player, false);
        node.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(this.phaseSeries::getCurrentPhase, teleportConsumer, this.scoreboard::addViewer));
        VoidConsumer checkRoundEnd = () -> RoundConditions.checkRoundEnd(this.phaseSeries, this.teamService);
        node.addListener(PlayerDisconnectEvent.class,
                new PlayerQuitListener(
                        this.phaseSeries::getCurrentPhase,
                        this.teamService.getTeams()::get,
                        checkRoundEnd,
                        this.scoreboard::removeViewer
                )
        );
        node.addListener(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        node.addListener(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(player -> {
        }, this.staminaService::getStaminaBar));
        node.addListener(PlayerChatEvent.class, new PlayerChatListener());
        node.addListener(MultiPlayerTeamEvent.class, new TeamActionListener());

        // Listener for rounds
        node.addListener(RoundStartEvent.class, new RoundStartListener(this.scoreboard, this.roundProvider));
    }
}
