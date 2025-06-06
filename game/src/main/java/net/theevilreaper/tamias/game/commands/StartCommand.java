package net.theevilreaper.tamias.game.commands;

import net.theevilreaper.xerus.api.phase.Phase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    private final Supplier<Phase> phaseSupplier;
    private final Component unableToStart = Messages.withPrefix(
            Component.text("Unable to start the game because the timer is to low!", NamedTextColor.RED)
    );

    public StartCommand(@NotNull Supplier<Phase> phaseSupplier) {
        super("start");
        this.phaseSupplier = phaseSupplier;
        this.setCondition(Conditions::playerOnly);
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
}
