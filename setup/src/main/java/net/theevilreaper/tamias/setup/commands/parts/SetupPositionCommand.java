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
import net.theevilreaper.aves.map.BaseMap;
import net.theevilreaper.aves.util.Components;
import net.theevilreaper.tamias.common.map.GameMap;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.setup.TamiasSetup;
import net.theevilreaper.tamias.setup.commands.type.SpawnType;
import net.theevilreaper.tamias.setup.data.InstanceSetupData;
import net.theevilreaper.tamias.setup.util.DirectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static net.theevilreaper.tamias.setup.util.SetupMessages.SELECT_MAP_FIRST;

public final class SetupPositionCommand extends Command {

    private final Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> setupDataFunction;

    public SetupPositionCommand(@NotNull Function<UUID, Optional<InstanceSetupData<? extends BaseMap>>> setupDataFunction) {
        super("position");
        this.setCondition(Conditions::playerOnly);
        this.setupDataFunction = setupDataFunction;
        String[] restrictions = SpawnType.getAsArray();
        ArgumentWord spawnType = ArgumentType.Word("spawnType").from(restrictions);
        this.addSyntax(this::handleSpawnSet, spawnType);
    }

    private void handleSpawnSet(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!sender.hasTag(TamiasSetup.SETUP_TAG)) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        String type = context.get("spawnType");

        SpawnType spawnType = SpawnType.fromString(type);

        if (spawnType == null) {
            sender.sendMessage("Invalid spawn type");
            return;
        }

        Optional<InstanceSetupData<? extends BaseMap>> setupData = setupDataFunction.apply(sender.identity().uuid());
        if (setupData.isEmpty()) {
            sender.sendMessage(SELECT_MAP_FIRST);
            return;
        }

        BaseMap map = setupData.get().getMap().get();
        Player player = (Player) sender;

        switch (map) {
            case GameMap gameMap -> this.handleGameSpawnSet(player, gameMap, spawnType);
            case BaseMap baseMap -> this.handleLobbySpawnSet(player, baseMap, spawnType);
        }
        Component posAsComponent = Components.convertPoint(Pos.fromPoint(player.getPosition()));
        Component argComponent = Component.text(type, NamedTextColor.GREEN);
        Component message = Messages.withPrefix(Component.text("The ", NamedTextColor.GRAY)
                .append(argComponent)
                .append(Component.space())
                .append(Component.text("position of the map is now located at: ", NamedTextColor.GRAY))
                .append(posAsComponent)
        );
        sender.sendMessage(message);
        setupData.get().triggerUpdate();
    }

    private void handleGameSpawnSet(@NotNull Player sender, @NotNull GameMap gameMap, @NotNull SpawnType spawnType) {
        Pos position = Pos.fromPoint(sender.getPosition());
        switch (spawnType) {
            case MAP_SPAWN, SPECTATOR -> gameMap.setSpawn(position);
            case BOMBER -> gameMap.setBomberInitialSpawn(position);
            case SURVIVOR -> this.handleSurvivorSpawnSet(sender, gameMap);
        }
    }

    /**
     * Handles the survivor spawn set.
     *
     * @param player  the player who executed the command
     * @param gameMap the map to set the spawn
     */
    private void handleSurvivorSpawnSet(@NotNull Player player, @NotNull GameMap gameMap) {
        Optional<Direction> determinedDirection = DirectionUtil.parseDirection(player);

        if (determinedDirection.isEmpty()) return;

        Pos pos = Pos.fromPoint(player.getPosition());
        Direction direction = determinedDirection.get();

        gameMap.setLeftSurvivorSpawn(pos, direction);

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
     * @param baseMap   the map to set the spawn
     * @param spawnType the type of spawn
     */
    private void handleLobbySpawnSet(@NotNull Player sender, @NotNull BaseMap baseMap, @NotNull SpawnType spawnType) {
        if (spawnType != SpawnType.MAP_SPAWN) {
            sender.sendMessage("A lobby map can only have a spawn");
            return;
        }
        baseMap.setSpawn(Pos.fromPoint(sender.getPosition()));
    }
}
