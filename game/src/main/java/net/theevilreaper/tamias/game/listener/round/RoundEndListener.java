package net.theevilreaper.tamias.game.listener.round;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.theevilreaper.aves.util.functional.VoidConsumer;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.game.round.RoundConditions;
import net.theevilreaper.tamias.game.round.event.RoundEndEvent;
import net.theevilreaper.tamias.game.util.EntityHelper;
import net.theevilreaper.tamias.game.util.FireworkUtil;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.tamias.game.util.SpectatorHelper;
import net.theevilreaper.xerus.api.component.team.ColorComponent;
import net.theevilreaper.xerus.api.team.Team;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RoundEndListener implements Consumer<RoundEndEvent> {

    private final Supplier<List<Team>> teamClear;
    private final VoidConsumer staminaCleanup;

    /**
     * Creates a new {@link RoundEndListener} that stops the stamina bars, announces the winner of the round, resets
     * spectators, shows every team member as a normal player again and clears all teams when a round ends.
     *
     * @param teamClear      a supplier that provides the list of teams to clear
     * @param staminaCleanup stops and removes the stamina bars of the round, so they don't keep running in the next one
     */
    public RoundEndListener(Supplier<List<Team>> teamClear, VoidConsumer staminaCleanup) {
        this.teamClear = teamClear;
        this.staminaCleanup = staminaCleanup;
    }

    @Override
    public void accept(RoundEndEvent event) {
        this.staminaCleanup.apply();
        List<Team> teams = teamClear.get();
        Optional<Team> survivorTeam = getTeam(teams, GameConfig.SURVIVOR_KEY);
        Optional<Team> bomberTeam = getTeam(teams, GameConfig.BOMBER_KEY);

        // The winner must be determined before the teams are cleared
        if (survivorTeam.isPresent() && bomberTeam.isPresent()) {
            celebrate(RoundConditions.getWinner(survivorTeam.get(), bomberTeam.get()), teams);
        }

        for (Team team : teams) {
            team.getPlayers().forEach(player -> {
                SpectatorHelper.disable(player);
                EntityHelper.switchToPlayer(player);
            });
            team.clearPlayers();
        }
    }

    /**
     * Announces the winner in the chat and launches a firework in the color of the winner for every player.
     *
     * @param winner the team which won the round
     * @param teams  the teams of the round
     */
    private void celebrate(Team winner, List<Team> teams) {
        Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers())
                .sendMessage(GameMessages.getWinMessage(winner));

        ColorComponent colorComponent = winner.get(ColorComponent.class);
        RGBLike fireworkColor = colorComponent != null ? colorComponent.colorData().getColor() : null;
        for (Team team : teams) {
            for (Player player : team.getPlayers()) {
                FireworkUtil.launch(player, fireworkColor);
            }
        }
    }

    private Optional<Team> getTeam(List<Team> teams, Key key) {
        return teams.stream().filter(team -> key.equals(team.key())).findFirst();
    }
}
