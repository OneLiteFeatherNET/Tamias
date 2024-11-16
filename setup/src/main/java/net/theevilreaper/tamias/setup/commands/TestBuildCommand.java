package net.theevilreaper.tamias.setup.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.area.GameArea;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.setup.data.SetupData;
import net.theevilreaper.tamias.setup.data.SetupDataService;
import org.jetbrains.annotations.NotNull;

//TODO: Remove this after some tests
public class TestBuildCommand extends Command {

    private final SetupDataService dataService;

    public TestBuildCommand(@NotNull SetupDataService dataService) {
        super("build");
        this.dataService = dataService;
        addSyntax(this::onCommand);
    }

    private void onCommand(CommandSender commandSender, CommandContext commandContext) {
        Player player = (Player) commandSender;
        SetupData setupData = dataService.getSetupData(player);
        if (!(setupData.getBaseMap() instanceof GameMap gameMap)) {
            return;
        }
        GameArea gameArea = new GameArea(player.getInstance(), gameMap.getGameAreaData());
        MapBuildPhase mapBuildPhase = new MapBuildPhase(() -> gameArea);
        mapBuildPhase.start();
    }
}
