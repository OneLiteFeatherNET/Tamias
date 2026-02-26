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
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Direction;
import net.onelitefeather.guira.data.SetupData;
import net.onelitefeather.guira.functional.OptionalSetupDataGetter;
import net.theevilreaper.aves.map.BaseMapBuilder;
import net.theevilreaper.aves.util.Components;
import net.theevilreaper.tamias.common.map.builder.GameMapBuilder;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.commands.type.SpawnType;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.data.LobbyData;
import net.theevilreaper.tamias.setup.util.DirectionUtil;
import net.theevilreaper.tamias.setup.util.SetupTags;

import java.util.Optional;

import static net.theevilreaper.tamias.setup.util.SetupMessages.SELECT_MAP_FIRST;

public final class SetupPositionCommand extends Command {

    private final OptionalSetupDataGetter setupDataFunction;

    public SetupPositionCommand(OptionalSetupDataGetter setupDataFunction) {
        super("position");
        this.setCondition(Conditions::playerOnly);
        this.setupDataFunction = setupDataFunction;
        String[] restrictions = SpawnType.getAsArray();
        ArgumentWord spawnType = ArgumentType.Word("spawnType").from(restrictions);
        this.addSyntax(this::handleSpawnSet, spawnType);
    }

    private void handleSpawnSet(CommandSender sender, CommandContext context) {
        if (!sender.hasTag(SetupTags.SETUP_TAG)) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        String type = context.get("spawnType");

        SpawnType spawnType = SpawnType.fromString(type);

        if (spawnType == null) {
            sender.sendMessage("Invalid spawn type");
            return;
        }

        Optional<SetupData> setupData = setupDataFunction.get(sender.identity().uuid());
        if (setupData.isEmpty()) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        SetupData data = setupData.get();
        Player player = (Player) sender;

        if (data instanceof LobbyData lobbyData) {
            this.handleLobbySpawnSet(player, lobbyData.getMapBuilder(), spawnType);
        } else {
            this.handleGameSpawnSet(player, ((InstanceSetupData)data).getMapBuilder(), spawnType);
        }
        Component posAsComponent = Components.convertPoint(player.getPosition());
        Component argComponent = Component.text(type, NamedTextColor.GREEN);
        Component message = Messages.withPrefix(Component.text("The ", NamedTextColor.GRAY)
                .append(argComponent)
                .append(Component.space())
                .append(Component.text("position of the map is now located at: ", NamedTextColor.GRAY))
                .append(posAsComponent)
        );
        sender.sendMessage(message);
        ((InstanceSetupData)data).triggerUpdate();
    }

    /**
     * Handles the game spawn set.
     *
     * @param sender    the player who executed the command
     * @param builder   the map builder to set the spawn
     * @param spawnType the type of spawn to set
     */
    private void handleGameSpawnSet(Player sender, BaseMapBuilder builder, SpawnType spawnType) {
        Pos position = sender.getPosition();
        switch (spawnType) {
            case MAP_SPAWN, SPECTATOR -> builder.spawn(position);
            case BOMBER -> ((GameMapBuilder)builder).bomberSpawn(position);
            case SURVIVOR -> this.handleSurvivorSpawnSet(sender, builder);
        }
    }

    /**
     * Handles the survivor spawn set.
     *
     * @param player  the player who executed the command
     * @param builder the map builder to set the spawn
     */
    private void handleSurvivorSpawnSet(Player player, BaseMapBuilder builder) {
        Optional<Direction> determinedDirection = DirectionUtil.parseDirection(player);

        if (determinedDirection.isEmpty()) return;

        Pos pos = player.getPosition();
        Direction direction = determinedDirection.get();

        ((GameMapBuilder)builder).spawnLayerDirection(direction);
        ((GameMapBuilder)builder).spawnLayerPos(pos);

        Component component = Messages.withPrefix(Component.text("Created round spawn at: ", NamedTextColor.GRAY)
                .append(Components.convertPoint(pos).style(Style.style(NamedTextColor.GOLD)))
                .append(Component.text(" with direction: ", NamedTextColor.GRAY))
                .append(Component.text(direction.name(), NamedTextColor.GREEN)));
        player.sendMessage(component);
    }

    /**
     * Handles the lobby spawn set.
     *
     * @param sender    the player who executed the command
     * @param builder   the map builder to set the spawn
     * @param spawnType the type of spawn to set
     */
    private void handleLobbySpawnSet(Player sender, BaseMapBuilder builder, SpawnType spawnType) {
        if (spawnType != SpawnType.MAP_SPAWN) {
            sender.sendMessage("A lobby map can only have a spawn");
            return;
        }
        builder.spawn(sender.getPosition().asPos());
    }
}
