package net.theevilreaper.tamias.setup.commands;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.theevilreaper.tamias.setup.commands.parts.SetupAreaCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupBuildersCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupNameCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupRoundSpawn;
import net.theevilreaper.tamias.setup.commands.parts.SetupSpawnCommand;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class SetupCommand extends Command {

    public SetupCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("setup");
        this.setCondition(Conditions::playerOnly);
        this.addSubcommand(new SetupNameCommand(mapFunction));
        this.addSubcommand(new SetupBuildersCommand(mapFunction));
        this.addSubcommand(new SetupSpawnCommand(mapFunction));
        this.addSubcommand(new SetupRoundSpawn(mapFunction));
        this.addSubcommand(new SetupAreaCommand(mapFunction));
    }
}
