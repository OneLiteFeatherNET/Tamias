package net.theevilreaper.tamias.setup.commands.parts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;

public final class SetupNameCommand extends Command {

    private final Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> setupDataFunction;

    public SetupNameCommand(@NotNull Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> setupDataFunction) {
        super("name");
        this.setupDataFunction = setupDataFunction;
        this.setCondition(Conditions::playerOnly);
        ArgumentString mapName = ArgumentType.String("mapName");
        this.addSyntax(this::handleNameSet, mapName);
    }

    private void handleNameSet(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.hasTag(TamiasSetup.SETUP_TAG)) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        String name = context.get("mapName");
        if (name == null || name.trim().isEmpty()) {
            sender.sendMessage("Please provide a valid name");
            return;
        }

        Optional<InstanceSetupData<? extends BaseMap>> setupData = this.setupDataFunction.apply(sender.identity().uuid());
        if (setupData.isEmpty()) {
            sender.sendMessage("An error occurred while setting up the map");
            return;
        }

        setupData.get().getMap().get().setName(name);
        Component message = Messages.withPrefix(Component.text("The name of the map now is: ", NamedTextColor.GRAY))
                .append(Component.text(name, NamedTextColor.AQUA));
        sender.sendMessage(message);
        setupData.get().triggerUpdate();
    }
}
