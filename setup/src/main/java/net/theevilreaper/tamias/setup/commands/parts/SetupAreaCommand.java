package net.theevilreaper.tamias.setup.commands.parts;

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
import net.onelitefeather.guira.data.SetupData;
import net.theevilreaper.aves.util.Components;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.GameData;
import net.theevilreaper.tamias.setup.util.DirectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static net.theevilreaper.tamias.setup.util.SetupMessages.SELECT_MAP_FIRST;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public class SetupAreaCommand extends Command {

    private final Function<UUID, Optional<SetupData>> setupDataFunction;
    private final ArgumentWord argumentWord;

    public SetupAreaCommand(@NotNull Function<UUID, Optional<SetupData>> setupDataFunction) {
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

        Optional<SetupData> setupData = this.setupDataFunction.apply(sender.identity().uuid());

        if (setupData.isEmpty()) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        GameData gameData = (GameData) setupData.get();
        gameData.swapAreaMode();

        sender.sendMessage("Area mode is now " + (gameData.hasAreaMode() ? "enabled" : "disabled"));

        if (gameData.hasAreaMode()) {
            sender.sendMessage("Please disable this mode after you have set the area");
        }
    }

    private void handlePositionSet(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.hasTag(TamiasSetup.SETUP_TAG)) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        Optional<SetupData> setupData = this.setupDataFunction.apply(sender.identity().uuid());

        if (setupData.isEmpty()) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        GameData gameData = (GameData) setupData.get();

        if (!gameData.hasAreaMode()) {
            sender.sendMessage("Area mode is not active");
            return;
        }

        String argument = context.get(argumentWord);


        switch (argument) {
            case "left" -> setLeftCorner((Player) sender, gameData);
            case "right" -> setRightCorner((Player) sender, gameData);
            default -> sender.sendMessage("Invalid argument");
        }
    }

    private void setLeftCorner(@NotNull Player player, @NotNull GameData setupData) {
        Optional<Direction> directionOptional = DirectionUtil.parseDirection(player);
        if (directionOptional.isEmpty()) return;

        Vec vec = Vec.fromPoint(player.getPosition()).sub(0, -1, 0);

        Direction direction = directionOptional.get();

        setupData.setLeftCorner(vec);
        setupData.setDirection(direction);

        Component component = Messages.withPrefix(Component.text("Left area corner is: ", NamedTextColor.GRAY)
                .append(Components.convertPoint(vec).style(Style.style(NamedTextColor.GOLD)))
                .append(Component.text(" with direction: ", NamedTextColor.GRAY))
                .append(Component.text(direction.name(), NamedTextColor.GREEN)));
        player.sendMessage(component);

    }

    private void setRightCorner(@NotNull Player player, @NotNull GameData setupData) {
        Vec vec = Vec.fromPoint(player.getPosition());

        setupData.setRightCorner(vec);
        Component component = Messages.withPrefix(Component.text("Right area corner is: ", NamedTextColor.GRAY)
                .append(Components.convertPoint(vec).style(Style.style(NamedTextColor.GOLD))));
        player.sendMessage(component);
    }
}
