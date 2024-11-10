package net.theevilreaper.tamias.game;

import com.google.gson.Gson;
import de.icevizion.aves.file.gson.PositionGsonAdapter;
import de.icevizion.xerus.api.phase.CyclicPhaseSeries;
import de.icevizion.xerus.api.phase.GamePhase;
import de.icevizion.xerus.api.phase.LinearPhaseSeries;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import de.icevizion.xerus.api.team.TeamServiceImpl;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.theevilreaper.tamias.common.ListenerHandling;
import net.theevilreaper.tamias.common.map.MapProvider;
import net.theevilreaper.tamias.game.commands.StartCommand;
import net.theevilreaper.tamias.game.commands.TestCommand;
import net.theevilreaper.tamias.game.config.GameConfig;
import net.theevilreaper.tamias.game.listener.PlayerBlockInteractListener;
import net.theevilreaper.tamias.game.listener.PlayerChatListener;
import net.theevilreaper.tamias.game.listener.PlayerJoinListener;
import net.theevilreaper.tamias.game.listener.PlayerQuitListener;
import net.theevilreaper.tamias.game.listener.PlayerSpawnListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileBlockListener;
import net.theevilreaper.tamias.game.listener.game.ProjectileEntityListener;
import net.theevilreaper.tamias.game.listener.game.RoundFinishListener;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.MapBuildPhase;
import net.theevilreaper.tamias.game.phase.PlayingPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.round.RoundData;
import net.theevilreaper.tamias.game.round.events.RoundFinishEvent;
import net.theevilreaper.tamias.game.stamina.StaminaService;
import net.theevilreaper.tamias.game.team.TamiasTeamCreator;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.BoardHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

import static de.icevizion.aves.inventory.util.InventoryConstants.CANCELLABLE_EVENT;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class Tamias extends Extension implements ListenerHandling {

    public static final Logger LOGGER = LoggerFactory.getLogger(Tamias.class);
    private final Gson gson;
    private final LinearPhaseSeries<GamePhase> phaseSeries;
    private final TeamService<Team> teamService;
    private final BoardHelper boardHelper;
    private final RoundData roundData;
    private final StaminaService staminaService;
    private MapProvider mapProvider;
    private TeamHelper teamDistributor;

    public Tamias() {
        this.phaseSeries = new LinearPhaseSeries<>();
        this.teamService = new TeamServiceImpl<>();
        this.initTeams();
        this.boardHelper = new BoardHelper();
        var posAdapter = new PositionGsonAdapter();
        this.gson = new Gson().newBuilder()
                .registerTypeAdapter(Pos.class, posAdapter)
                .registerTypeAdapter(Vec.class, posAdapter)
                .create();
        this.roundData = new RoundData();
        this.staminaService = new StaminaService();
    }

    @Override
    public void initialize() {
        checkMapDirectory();
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.enableAutoChunkLoad(true);
        registerCancelListener(MinecraftServer.getGlobalEventHandler());
        MinecraftServer.getInstanceManager().registerInstance(instance);

        MinecraftServer.getCommandManager().register(new StartCommand(this.phaseSeries::getCurrentPhase));

        this.mapProvider = new MapProvider(gson, getDataDirectory(), instance);
        this.mapProvider = new MapProvider(this.gson, getDataDirectory(), instance);
        this.teamDistributor = new TeamHelper(this.mapProvider, this.teamService.getTeams()::get);

        MinecraftServer.getInstanceManager().registerInstance(instance);
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> event.setSpawningInstance(instance));
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.CREATIVE);
            if (event.getSpawnInstance().getUniqueId().equals(instance.getUniqueId())) {
                event.getPlayer().teleport(new Pos(-11, 130, 25));
            }

            ((LobbyPhase) this.phaseSeries.getCurrentPhase()).checkStartCondition();
        });

        MinecraftServer.getCommandManager().register(new TestCommand());
        this.createPhaseStructure();
        registerCancelListener(MinecraftServer.getGlobalEventHandler());

        registerListener(instance, MinecraftServer.getGlobalEventHandler());
        this.phaseSeries.start();
    }

    @Override
    public void terminate() {

    }

    private void createPhaseStructure() {
        this.phaseSeries.add(new LobbyPhase(this.mapProvider));
        var gamePhaseSeries = new CyclicPhaseSeries<GamePhase>("game");
        gamePhaseSeries.add(new MapBuildPhase(this.mapProvider::getGameArea));
        gamePhaseSeries.add(new PlayingPhase(this.boardHelper::updateTitle));
        gamePhaseSeries.setMaxIterations(GameConfig.GAME_ROUNDS);
        this.phaseSeries.addAll(gamePhaseSeries);
        this.phaseSeries.add(new RestartPhase());
    }

    private void checkMapDirectory() {
        var dataDirectory = getDataDirectory();
        var mapDirectory = dataDirectory.resolve(GameConfig.MAP_PATH_NAME);
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
        var teamCreator = new TamiasTeamCreator();
        this.teamService.add(Team.builder(teamCreator).name("Survivor").capacity(16).build());
        this.teamService.add(Team.builder(teamCreator).name("Bomber").capacity(16).build());
    }

    void registerGameListener(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(RoundFinishEvent.class, new RoundFinishListener(this.teamDistributor));
    }

    void registerListener(@NotNull Instance instance, @NotNull EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, new PlayerJoinListener(this.phaseSeries::getCurrentPhase, () -> null));
        eventNode.addListener(PlayerSpawnEvent.class, new PlayerSpawnListener(instance.getUniqueId(), this.phaseSeries));
        eventNode.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(this.phaseSeries));
        eventNode.addListener(ProjectileCollideWithBlockEvent.class, new ProjectileBlockListener());
        eventNode.addListener(ProjectileCollideWithEntityEvent.class, new ProjectileEntityListener(this.teamDistributor, this.staminaService::getStaminaBar));
        eventNode.addListener(PlayerChatEvent.class, new PlayerChatListener());
    }
}
