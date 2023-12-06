package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.util.ComponentHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;

public final class SetupSpawnCommand extends Command {

    public SetupSpawnCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("spawn");

        addSyntax((sender, context) -> {
            var map = mapFunction.apply(sender);
            if (map == null) {
                sender.sendMessage(SELECT_MAP_FIRST);
                return;
            }
            Pos position = Pos.fromPoint(((Player) sender).getPosition());
            map.setSpawn(position);
            var posAsComponent = ComponentHelper.convertPointToComponent(position);
            sender.sendMessage(Component.text("The spawn position of the map is now located at: ").append(posAsComponent));
        });
    }
}
