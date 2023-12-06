package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.theevilreaper.tamias.util.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.theevilreaper.tamias.setup.SetupValidations.argCondition;
import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;


public final class SetupNameCommand extends Command {

    public SetupNameCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("name");
        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            var map = mapFunction.apply(sender);

            if (map == null) {
                sender.sendMessage(SELECT_MAP_FIRST);
                return;
            }
            sender.sendMessage(Messages.withMini("<red>Please provide a <gray>valid <red>name for the map"));
        });
        var mapName = ArgumentType.String("mapName");

        addSyntax((sender, context) -> {
            var map = mapFunction.apply(sender);

            if (map == null) {
                sender.sendMessage(SELECT_MAP_FIRST);
                return;
            }
            var name = context.get(mapName);
            map.setName(name);
            sender.sendMessage("The name of the map now is: " + name);
        }, mapName);
    }
}
