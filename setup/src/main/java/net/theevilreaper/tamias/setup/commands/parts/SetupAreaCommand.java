package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.util.Components;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.theevilreaper.tamias.common.util.DirectionFaceHelper;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.GameData;
import net.theevilreaper.tamias.setup.data.SetupData;
import net.theevilreaper.tamias.setup.util.SetupMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public class SetupAreaCommand extends Command {

    private final Function<Player, SetupData> setupDataFunction;
    private final ArgumentWord argumentWord;

    public SetupAreaCommand(@NotNull Function<Player, SetupData> setupDataFunction) {
        super("area");
        this.setCondition(Conditions::playerOnly);
        this.setupDataFunction = setupDataFunction;
        this.argumentWord = ArgumentType.Word("argument").from("left", "right");
        this.addSyntax(this::handlePositionSet, argumentWord);


        ArgumentWord stateChange = ArgumentType.Word("switch");
        this.addSyntax(this::handleStateChange, stateChange);
    }

    private void handleStateChange(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.hasTag(TamiasSetup.SETUP_TAG)) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        SetupData setupData = this.setupDataFunction.apply((Player) sender);

        if (setupData == null) {
            sender.sendMessage("An error occurred while setting up the map");
            return;
        }

        setupData.swapAreaMode();

        sender.sendMessage("Area mode is now " + (setupData.hasAreaMode() ? "enabled" : "disabled"));

        if (setupData.hasAreaMode()) {
            sender.sendMessage("Please disable this mode after you have set the area");
        }
    }

    private void handlePositionSet(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.hasTag(TamiasSetup.SETUP_TAG)) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        SetupData setupData = this.setupDataFunction.apply((Player) sender);

        if (setupData == null) {
            sender.sendMessage("An error occurred while setting up the map");
            return;
        }

        if (!setupData.hasAreaMode()) {
            sender.sendMessage("Area mode is not active");
            return;
        }

        String argument = context.get(argumentWord);


        switch (argument) {
            case "left" -> setLeftCorner((Player) sender, setupData);
            case "right" -> setRightCorner((Player) sender, setupData);
            default -> sender.sendMessage("Invalid argument");
        }
    }

    private void setLeftCorner(@NotNull Player player, @NotNull SetupData setupData) {
        Direction direction = MathUtils.getHorizontalDirection(player.getPosition().yaw());

        Vec dir = player.getPosition().direction();

        double directionPitch = Math.toDegrees(-Math.atan2(dir.y(), Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z())));

        if (!DirectionFaceHelper.isValidFace(directionPitch)) {
            String indirectDirection = DirectionFaceHelper.getInvalidDirection(directionPitch).name();
            player.sendMessage(SetupMessages.getInvalidFace(indirectDirection));
            return;
        }
        Vec vec = Vec.fromPoint(player.getPosition()).sub(0, -1,0);

        GameData gameData = ((GameData) setupData);

        gameData.setLeftCorner(vec);
        gameData.setDirection(direction);

        Component component = Messages.withPrefix(Component.text("Left area corner is: ", NamedTextColor.GRAY)
                .append(Components.convertPoint(vec).style(Style.style(NamedTextColor.GOLD)))
                .append(Component.text(" with direction: ", NamedTextColor.GRAY))
                .append(Component.text(direction.name(), NamedTextColor.GREEN)));
        player.sendMessage(component);

    }

    private void setRightCorner(@NotNull Player player, @NotNull SetupData setupData) {
        Vec vec = Vec.fromPoint(player.getPosition());

        GameData gameData = ((GameData) setupData);

        gameData.setRightCorner(vec);
        Component component = Messages.withPrefix(Component.text("Right area corner is: ", NamedTextColor.GRAY)
                .append(Components.convertPoint(vec).style(Style.style(NamedTextColor.GOLD))));
        player.sendMessage(component);
    }
}
