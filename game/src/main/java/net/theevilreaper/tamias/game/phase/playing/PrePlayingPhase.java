package net.theevilreaper.tamias.game.phase.playing;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.theevilreaper.tamias.game.stamina.event.StaminaCreateEvent;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.xerus.api.component.team.ColorComponent;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.theevilreaper.xerus.api.team.TeamService;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

/**
 * The {@link PrePlayingPhase} deals each code logic that should be executed before the {@link PlayingPhase} begins.
 * It reduces the complexity of the playing phase without dealing too much overhead.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PrePlayingPhase extends TimedPhase {

    private final TeamService teamService;

    /**
     * Creates a new instance from the phase
     *
     * @param teamService the service that provides access to the teams
     */
    public PrePlayingPhase(@NotNull TeamService teamService) {
        super("Pre-Playing", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(5);
        this.setTickDirection(TickDirection.DOWN);
        this.teamService = teamService;
    }

    @Override
    public void onStart() {
        super.onStart();
        TeamHelper.allocateTeams(this.teamService);
    }

    @Override
    protected void onFinish() {
        this.teamService.getTeams().forEach(team -> {
            ColorComponent colorComponent = team.get(ColorComponent.class);
            if (colorComponent != null) {
                for (Player player : team.getPlayers()) {
                    Component displayName = Component.text(player.getUsername(), colorComponent.colorData().getChatColor());
                    player.setDisplayName(displayName);
                }
            }
        });

        EventDispatcher.call(new StaminaCreateEvent());
    }

    @Override
    public void onUpdate() {
        // Nothing to do here at the moment
    }
}
