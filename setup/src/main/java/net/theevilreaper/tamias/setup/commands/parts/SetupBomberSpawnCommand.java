package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.map.GameMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;

public final class SetupBomberSpawnCommand extends Command {

    public SetupBomberSpawnCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("bomber");

        addSyntax((sender, context) -> {
            var map = mapFunction.apply(sender);

            if (map == null) {
                sender.sendMessage(SELECT_MAP_FIRST);
                return;
            }

            if (!(map instanceof GameMap gameMap)) {
                sender.sendMessage("This map is not a game map");
                return;
            }
            gameMap.setBomberInitialSpawn(Pos.fromPoint(((Player) sender).getPosition()));
            sender.sendMessage("The bomber spawn position of the map is now located at: " + gameMap.getBomberInitialSpawn());
        });
    }
}
