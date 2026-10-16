package net.theevilreaper.tamias.game.listener;

import net.kyori.adventure.key.Key;
import net.theevilreaper.aves.util.functional.PlayerConsumer;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.xerus.api.phase.Phase;
import net.theevilreaper.xerus.api.team.Team;
import net.kyori.adventure.audience.Audience;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.phase.LobbyPhase;
import net.theevilreaper.tamias.game.phase.RestartPhase;
import net.theevilreaper.tamias.game.phase.playing.PlayingPhase;
import net.theevilreaper.tamias.game.util.GameMessages;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.MinecraftServer.getConnectionManager;

/**
 * The listener implementation handles the {@link PlayerDisconnectEvent} call, when a player disconnects.
 * This is required to perform operations on different states of the game.
 * As example the quit logic in the {@link LobbyPhase} is different compared to the {@link PlayingPhase}
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public final class PlayerQuitListener implements Consumer<PlayerDisconnectEvent> {

    /**
     * A round needs at least one bomber and one survivor.
     */
    private static final int MIN_GAME_PLAYERS = 2;

    private final Supplier<Phase> phaseSupplier;
    private final Function<Key, Team> teamFunction;
    private final PlayerConsumer staminaRemover;
    private final VoidConsumer roundEndCheck;
    private final PlayerConsumer scoreboardConsumer;
    private final VoidConsumer earlyGameFinish;

    /**
     * Creates a new {@link PlayerQuitListener}.
     *
     * @param phaseSupplier      provides the active phase
     * @param teamFunction       resolves a team by its key
     * @param staminaRemover     stops and removes the stamina bar of a player
     * @param roundEndCheck      checks if the round is over
     * @param scoreboardConsumer removes the player from the scoreboard
     * @param earlyGameFinish    ends the game when too few players are left
     */
    public PlayerQuitListener(
            Supplier<Phase> phaseSupplier,
            Function<Key, Team> teamFunction,
            PlayerConsumer staminaRemover,
            VoidConsumer roundEndCheck,
            PlayerConsumer scoreboardConsumer,
            VoidConsumer earlyGameFinish
    ) {
        this.phaseSupplier = phaseSupplier;
        this.teamFunction = teamFunction;
        this.staminaRemover = staminaRemover;
        this.roundEndCheck = roundEndCheck;
        this.scoreboardConsumer = scoreboardConsumer;
        this.earlyGameFinish = earlyGameFinish;
    }

    @Override
    public void accept(PlayerDisconnectEvent event) {
        Phase phase = phaseSupplier.get();

        if (phase == null) return;

        Player player = event.getPlayer();
        // The restart phase kicks every player, so there is nothing left to handle
        if (phase instanceof RestartPhase) return;

        Audience.audience(getConnectionManager().getOnlinePlayers()).sendMessage(GameMessages.getLeaveMessage(player));
        this.scoreboardConsumer.accept(player);

        if (phase instanceof LobbyPhase lobbyPhase) {
            lobbyPhase.checkStopCondition(player);
            return;
        }

        this.handleGameQuit(player);
    }

    /**
     * Handles the quit logic for all phases of a round. The player is removed from the team and the stamina bar
     * is stopped, otherwise the bar keeps running and fires events for a player who is no longer online.
     *
     * @param player the involved player
     */
    private void handleGameQuit(Player player) {
        this.removeFromTeam(player);
        this.staminaRemover.accept(player);

        if (getRemainingPlayers(player) < MIN_GAME_PLAYERS) {
            this.earlyGameFinish.apply();
            return;
        }

        this.roundEndCheck.apply();
    }

    private void removeFromTeam(Player player) {
        if (!player.hasTag(Tags.TEAM_KEY)) return;

        Team team = this.teamFunction.apply(Key.key(player.getTag(Tags.TEAM_KEY)));
        player.removeTag(Tags.TEAM_KEY);

        if (team == null) return;
        team.removePlayer(player);
    }

    /**
     * Returns the amount of online players without the leaving player. On a regular disconnect Minestom removes the
     * player from the online players before the disconnect event is called. The filter only applies when a player
     * is removed directly.
     *
     * @param leavingPlayer the player who leaves the game
     * @return the amount of remaining players
     */
    private long getRemainingPlayers(Player leavingPlayer) {
        return getConnectionManager().getOnlinePlayers().stream()
                .filter(player -> player != leavingPlayer)
                .count();
    }
}
