package net.theevilreaper.tamias.game.commands;

import net.theevilreaper.xerus.api.phase.Phase;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.condition.Conditions;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Messages;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * The command allows to force start the game.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class StartCommand extends Command {

    private static final String PERMISSION = "tamias.command.start";

    private final Supplier<Phase> phaseSupplier;
    private final Component unableToStart = Messages.withPrefix(
            Component.text("Unable to start the game because the timer is to low!", NamedTextColor.RED)
    );

    public StartCommand(@NotNull Supplier<Phase> phaseSupplier) {
        super("start");
        this.phaseSupplier = phaseSupplier;
        this.setCondition((sender, commandString) -> Conditions.playerOnly(sender, commandString) && hasStartPermission(sender));
        addSyntax(this::forceStart);
    }

    private void forceStart(@NotNull CommandSender sender, @NotNull CommandContext commandContext) {
        Phase phase = phaseSupplier.get();

        if (!(phase instanceof LobbyPhase lobbyPhase)) return;

        if (lobbyPhase.isPaused()) {
            sender.sendMessage(GameMessages.PHASE_NOT_RUNNING);
            return;
        }

        if (lobbyPhase.getCurrentTicks() < GameConfig.FORCE_START_TIME + 1) {
            sender.sendMessage(unableToStart);
            return;
        }

        if (lobbyPhase.isForceStarted()) {
            sender.sendMessage(GameMessages.ALREADY_FORCE_STARTED);
            return;
        }

        lobbyPhase.setForceStarted(true);
        sender.sendMessage(GameMessages.PHASE_FORCE_STARTED);
    }

    /**
     * Reads Adventure's {@link PermissionChecker#POINTER}, which the player implementation backs with LuckPerms.
     * A sender without that pointer is denied.
     *
     * @param sender the sender to check
     * @return {@code true} if the sender holds {@value #PERMISSION}
     */
    private static boolean hasStartPermission(CommandSender sender) {
        return sender.getOrDefault(PermissionChecker.POINTER, PermissionChecker.always(TriState.FALSE))
                .test(PERMISSION);
    }
}
