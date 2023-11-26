package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.theevilreaper.tamias.setup.SetupValidations.argCondition;


public final class SetupNameCommand extends Command {


    public SetupNameCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("name");
        setCondition(Conditions::playerOnly);

        var mapName = ArgumentType.String("mapName");
        addSyntax((sender, context) -> {
            var name = context.get(mapName);
            var map = mapFunction.apply(sender);

            if (map == null) return;
            if (argCondition(name.trim().isEmpty(), sender, Component.text("An empty name is not allowed", NamedTextColor.RED))) return;
            map.setName(name);
            sender.sendMessage("The name of the map now is: " + name);
        }, mapName);
    }
}
