package net.theevilreaper.tamias.setup.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public class GameModeCommand extends Command {

    public GameModeCommand() {
        super("gamemode", "gm");
        ArgumentWord argumentWord = ArgumentType.Word("gameMode").from("creative", "spectator");


        addSyntax((sender, context) -> {
            @Nullable String gameMode = context.get("gameMode");
            
            if (gameMode == null) {
                sender.sendMessage("Please specify a valid game mode.");
                return;
            }

            Player player = ((Player) sender);
            switch (gameMode.toLowerCase()) {
                case "creative":
                    player.setGameMode(GameMode.CREATIVE);
                    sender.sendMessage("Game mode set to Creative.");
                    break;
                case "spectator":
                    player.setGameMode(GameMode.SPECTATOR);
                    sender.sendMessage("Game mode set to Spectator.");
                    break;
                default:
                    sender.sendMessage("Invalid game mode. Use 'creative' or 'spectator'.");
            }
            
        }, argumentWord);
    }
}
