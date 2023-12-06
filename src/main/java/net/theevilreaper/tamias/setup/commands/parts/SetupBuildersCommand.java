package net.theevilreaper.tamias.setup.commands.parts;

import de.icevizion.aves.map.BaseMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static net.theevilreaper.tamias.setup.TamiasSetup.SELECT_MAP_FIRST;

/**
 * The command allows the set the creators of a map to a {@link de.icevizion.aves.map.BaseMap} reference.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SetupBuildersCommand extends Command {

    public SetupBuildersCommand(@NotNull Function<CommandSender, BaseMap> mapFunction) {
        super("builders");
        setCondition(Conditions::playerOnly);

        var buildersArray = ArgumentType.StringArray("builders");

        addSyntax((sender, context) -> {
            String[] builders = context.get(buildersArray);

            var map = mapFunction.apply(sender);

            if (map == null) {
                sender.sendMessage(SELECT_MAP_FIRST);
                return;
            }

            if (builders.length == 0) {
                sender.sendMessage(Component.text("A map needs at least one builder", NamedTextColor.RED));
                return;
            }
            var buildersAsComponent = Component.join(JoinConfiguration.arrayLike(), transformBuilders(builders));
            sender.sendMessage(Component.text("The creators of the map are: ").append(buildersAsComponent));
        }, buildersArray);
    }

    private @NotNull List<TextComponent> transformBuilders(@NotNull String... builders) {
        return Arrays.stream(builders).map(Component::text).toList();
    }
}
