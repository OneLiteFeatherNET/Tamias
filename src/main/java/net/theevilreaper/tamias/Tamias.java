package net.theevilreaper.tamias;

import de.icevizion.xerus.api.phase.CyclicPhaseSeries;
import de.icevizion.xerus.api.phase.GamePhase;
import de.icevizion.xerus.api.phase.LinearPhaseSeries;
import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.TeamService;
import de.icevizion.xerus.api.team.TeamServiceImpl;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.Instance;
import net.theevilreaper.tamias.commands.TestCommand;
import net.theevilreaper.tamias.config.GameConfig;
import net.theevilreaper.tamias.listener.PlayerQuitListener;
import net.theevilreaper.tamias.phase.LobbyPhase;
import net.theevilreaper.tamias.phase.MapBuildPhase;
import net.theevilreaper.tamias.phase.PlayingPhase;
import net.theevilreaper.tamias.phase.RestartPhase;
import org.jetbrains.annotations.NotNull;

import static de.icevizion.aves.inventory.util.InventoryConstants.CANCELLABLE_EVENT;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class Tamias extends Extension {

    private final LinearPhaseSeries<GamePhase> phaseSeries;
    private final TeamService<Team> teamService;

    public Tamias() {
        this.phaseSeries = new LinearPhaseSeries<>();
        this.teamService = new TeamServiceImpl<>();
        this.initTeams();
    }

    @Override
    public void initialize() {
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        MinecraftServer.getInstanceManager().registerInstance(instance);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(instance);
        });
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
           event.getPlayer().teleport(new Pos(0, 150, 0));
        });

        MinecraftServer.getCommandManager().register(new TestCommand());


        this.createPhaseStructure();

        registerCancelListener(MinecraftServer.getGlobalEventHandler());
    }

    @Override
    public void terminate() {

    }

    private void createPhaseStructure() {
        this.phaseSeries.add(new LobbyPhase());

        var gamePhaseSeries = new CyclicPhaseSeries<GamePhase>("game");
        gamePhaseSeries.add(new MapBuildPhase());
        gamePhaseSeries.add(new PlayingPhase());
        gamePhaseSeries.setMaxIterations(GameConfig.GAME_ROUNDS);
        this.phaseSeries.addAll(gamePhaseSeries);
        this.phaseSeries.add(new RestartPhase());
    }

    private void initTeams() {
      /*  var teamCreator = new TamiasTeamCreator();
        this.teamService.add(Team.builder(teamCreator).name("Survivor").capacity(16).build());
        this.teamService.add(Team.builder(teamCreator).name("Bomber").capacity(16).build());*/
    }

    void registerListener(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerDisconnectEvent.class, new PlayerQuitListener(this.phaseSeries));
    }


    void registerCancelListener(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockBreakEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(PlayerBlockPlaceEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(ItemDropEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(PlayerSwapItemEvent.class, CANCELLABLE_EVENT::accept);
        eventNode.addListener(PlayerBlockInteractEvent.class, CANCELLABLE_EVENT::accept);
    }
}
