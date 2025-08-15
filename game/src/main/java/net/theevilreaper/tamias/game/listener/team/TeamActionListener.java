package net.theevilreaper.tamias.game.listener.team;

import net.theevilreaper.xerus.api.team.Team;
import net.theevilreaper.xerus.api.team.event.MultiPlayerTeamEvent;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class TeamActionListener implements Consumer<MultiPlayerTeamEvent> {

    @Override
    public void accept(@NotNull MultiPlayerTeamEvent event) {
        switch (event.getAction()) {
            case ADD -> this.handleQuit(event.getTeam());
            case REMOVE -> this.handleQuit(event.getTeam());
        }
    }

    private void handleQuit(@NotNull Team team) {
        for (Player teamPlayer : team.getPlayers()) {
            teamPlayer.getInventory().clear();
        }
    }
}
