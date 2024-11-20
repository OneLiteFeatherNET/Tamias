package net.theevilreaper.tamias.game.listener.team;

import de.icevizion.xerus.api.team.Team;
import de.icevizion.xerus.api.team.event.MultiPlayerTeamEvent;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

public final class TeamMultiRemoveListener implements Consumer<MultiPlayerTeamEvent<Team>> {

    @Override
    public void accept(@NotNull MultiPlayerTeamEvent<Team> event) {
        Set<Player> teamPlayers = event.getTeam().getPlayers();
        for (Player teamPlayer : teamPlayers) {
            teamPlayer.getInventory().clear();
        }
    }
}
