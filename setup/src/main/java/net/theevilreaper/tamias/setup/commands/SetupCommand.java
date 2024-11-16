package net.theevilreaper.tamias.setup.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.theevilreaper.tamias.setup.commands.parts.SetupAreaCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupBuildersCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupNameCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupPositionCommand;
import net.theevilreaper.tamias.setup.data.SetupDataService;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link SetupCommand} is the main command for the setup process.
 * It contains all subcommands to configure the game.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SetupCommand extends Command {

    public SetupCommand(@NotNull SetupDataService dataService) {
        super("setup");
        this.setCondition(Conditions::playerOnly);
        this.addSubcommand(new SetupNameCommand(dataService::getSetupData));
        this.addSubcommand(new SetupBuildersCommand(dataService::getSetupData));
        this.addSubcommand(new SetupPositionCommand(dataService::getSetupData));
        this.addSubcommand(new SetupAreaCommand(dataService::getSetupData));
    }
}
