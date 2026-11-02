package net.theevilreaper.tamias.setup.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.common.util.Messages;
import org.jetbrains.annotations.Nullable;

public class GameModeCommand extends Command {

    public GameModeCommand() {
        super("gamemode", "gm");
        ArgumentWord argumentWord = ArgumentType.Word("gameMode").from("creative", "spectator");


        addSyntax((sender, context) -> {
            @Nullable String gameMode = context.get("gameMode");
            
            if (gameMode == null) {
                sender.sendMessage(Messages.withPrefix(Component.text("Please specify a game mode: creative or spectator.", NamedTextColor.RED)));
                return;
            }

            Player player = ((Player) sender);
            switch (gameMode.toLowerCase()) {
                case "creative":
                    player.setGameMode(GameMode.CREATIVE);
                    sender.sendMessage(Messages.withPrefix(Component.text("Your game mode is now creative.", NamedTextColor.GREEN)));
                    break;
                case "spectator":
                    player.setGameMode(GameMode.SPECTATOR);
                    sender.sendMessage(Messages.withPrefix(Component.text("Your game mode is now spectator.", NamedTextColor.GREEN)));
                    break;
                default:
                    sender.sendMessage(Messages.withPrefix(Component.text("Unknown game mode, use creative or spectator.", NamedTextColor.RED)));
            }
            
        }, argumentWord);
    }
}
