package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.map.GameMap;
import net.theevilreaper.tamias.setup.TamiasSetup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

public class SetupAreaCommand extends Command {

    private final Function<CommandSender, BaseMap> mapFunction;
    private final ArgumentWord argumentWord;

    public SetupAreaCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("area");
        this.mapFunction = mapFunction;
        this.argumentWord = ArgumentType.Word("argument").from("left", "right");
        setCondition(Conditions::playerOnly);
        addSyntax(this::handleCommand, argumentWord);
    }

    private void handleCommand(@NotNull CommandSender sender, @NotNull CommandContext context) {
        var map = mapFunction.apply(sender);
        if (map == null) {
            sender.sendMessage(TamiasSetup.SELECT_MAP_FIRST);
            return;
        }

        if (!(map instanceof GameMap gameMap)) {
            sender.sendMessage("This map is not a game map");
            return;
        }

        var argument = context.get(argumentWord);
        var player = (Player) sender;
        if ("left".equals(argument)) {
            gameMap.setLeftAreaPos(Vec.fromPoint(player.getPosition()));
        } else {
            gameMap.setRightArePos(Vec.fromPoint(player.getPosition()));
        }
    }
}
