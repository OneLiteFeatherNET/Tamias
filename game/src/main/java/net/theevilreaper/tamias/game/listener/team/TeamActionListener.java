package net.theevilreaper.tamias.game.listener.team;

import net.theevilreaper.xerus.api.team.event.MultiPlayerTeamEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class TeamActionListener implements Consumer<MultiPlayerTeamEvent> {

    @Override
    public void accept(@NotNull MultiPlayerTeamEvent event) {
        // MultiPlayerTeamEvent handling logic if needed
    }
}
