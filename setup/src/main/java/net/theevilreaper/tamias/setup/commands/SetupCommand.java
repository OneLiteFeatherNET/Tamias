package net.theevilreaper.tamias.setup.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.onelitefeather.guira.SetupDataService;
import net.theevilreaper.tamias.setup.commands.parts.SetupAreaCommand;
import net.theevilreaper.tamias.setup.commands.parts.SetupPositionCommand;

/**
 * The {@link SetupCommand} is the main command for the setup process.
 * It contains all subcommands to configure the game.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SetupCommand extends Command {

    public SetupCommand(SetupDataService dataService) {
        super("setup");
        this.setCondition(Conditions::playerOnly);
        this.addSubcommand(new SetupPositionCommand(dataService::get));
        this.addSubcommand(new SetupAreaCommand(dataService::get));
    }
}
