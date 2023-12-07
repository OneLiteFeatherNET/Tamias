package net.theevilreaper.tamias.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.theevilreaper.tamias.phase.MapBuildPhase;
import org.jetbrains.annotations.NotNull;

public class TestBuildCommand extends Command {
    MapBuildPhase mapBuildPhase;

    public TestBuildCommand(@NotNull MapBuildPhase mapBuildPhase) {
        super("build");
        this.mapBuildPhase = mapBuildPhase;
        addSyntax(this::onCommand);
    }

    private void onCommand(CommandSender commandSender, CommandContext commandContext) {
        mapBuildPhase.start();
    }

}
