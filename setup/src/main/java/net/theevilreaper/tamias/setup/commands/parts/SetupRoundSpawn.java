package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.MathUtils;
import net.theevilreaper.tamias.common.map.GameMap;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;

import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;

public class SetupRoundSpawn extends Command {

    private final Function<CommandSender, BaseMap> mapFunction;

    public SetupRoundSpawn(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("round");
        this.mapFunction = mapFunction;
        addSyntax(this::onCommand);
    }

    private void onCommand(@NotNull CommandSender sender, @NotNull CommandContext commandContext) {
        var player = (Player) sender;
        var gameMap = ((GameMap) this.mapFunction.apply(sender));

        if (gameMap == null) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }


        var dir = player.getPosition().direction();
        var directionPitch = Math.toDegrees(-Math.atan2(dir.y(), Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z())));

        /*if (!Helper.isValidFace(directionPitch)) {
            player.sendMessage(Component.text("You are not looking in the right direction", NamedTextColor.RED));
            return;
        }*/

        var direction = MathUtils.getHorizontalDirection(player.getPosition().yaw());
        gameMap.setLeftSurvivorSpawn(Pos.fromPoint(player.getPosition()));
        gameMap.setDirection(direction.name().toLowerCase(Locale.ROOT));
    }
}
