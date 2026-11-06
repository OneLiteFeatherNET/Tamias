package net.theevilreaper.tamias.game.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.onelitefeather.tamias.stats.StatsRepository;
import net.onelitefeather.tamias.stats.player.PlayerStatsView;
import net.theevilreaper.tamias.game.util.GameMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Shows the statistics of a player over every stored round. {@code /stats} shows the own statistics,
 * {@code /stats <player>} the ones of another player, online or not.
 * <p>
 * The lookup runs on the thread of the {@link StatsRepository}, the answer is sent when it is done.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class StatsCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsCommand.class);

    private final StatsRepository repository;

    /**
     * Creates a new {@link StatsCommand}.
     *
     * @param repository the repository to read the statistics from
     */
    public StatsCommand(StatsRepository repository) {
        super("stats");
        this.repository = repository;

        ArgumentWord target = ArgumentType.Word("player");
        target.setSuggestionCallback((_, _, suggestion) -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                suggestion.addEntry(new SuggestionEntry(player.getUsername()));
            }
        });

        setDefaultExecutor(this::showOwn);
        addSyntax(this::showOther, target);
    }

    private void showOwn(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GameMessages.STATS_PLAYER_ONLY);
            return;
        }
        if (!this.repository.enabled()) {
            sender.sendMessage(GameMessages.STATS_DISABLED);
            return;
        }
        reply(sender, player.getUsername(), this.repository.findTotals(player.getUuid()));
    }

    private void showOther(CommandSender sender, CommandContext context) {
        String name = context.get("player");
        if (!this.repository.enabled()) {
            sender.sendMessage(GameMessages.STATS_DISABLED);
            return;
        }

        Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name);
        if (online != null) {
            reply(sender, online.getUsername(), this.repository.findTotals(online.getUuid()));
            return;
        }

        reply(sender, name, this.repository.findPlayerByName(name).thenCompose(player -> player
                .map(found -> this.repository.findTotals(found.uuid()))
                .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))));
    }

    private static void reply(CommandSender sender, String name, CompletableFuture<Optional<PlayerStatsView>> lookup) {
        lookup.whenComplete((totals, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to load the statistics of {}", name, throwable);
                sender.sendMessage(GameMessages.STATS_FAILED);
                return;
            }
            sender.sendMessage(totals.map(GameMessages::getStatsMessage)
                    .orElseGet(() -> GameMessages.getStatsNotFoundMessage(name)));
        });
    }
}
