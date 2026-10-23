package net.theevilreaper.tamias.game.phase.playing;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.sound.SoundEvent;
import net.theevilreaper.tamias.game.stamina.event.StaminaCreateEvent;
import net.theevilreaper.tamias.game.team.TeamHelper;
import net.theevilreaper.tamias.game.util.GameMessages;
import net.theevilreaper.xerus.api.component.team.ColorComponent;
import net.theevilreaper.xerus.api.phase.TickDirection;
import net.theevilreaper.xerus.api.phase.TimedPhase;
import net.theevilreaper.xerus.api.team.TeamService;

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

    private static final Sound PLING = Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_BELL, Sound.Source.MASTER, 1.0f, 1.0f);

    private final TeamService teamService;

    /**
     * Creates a new instance from the phase
     *
     * @param teamService the service that provides access to the teams
     */
    public PrePlayingPhase(TeamService teamService) {
        super("Pre-Playing", ChronoUnit.SECONDS, 1);
        this.setCurrentTicks(5);
        this.setTickDirection(TickDirection.DOWN);
        this.teamService = teamService;
    }

    @Override
    public void onStart() {
        this.setCurrentTicks(5);
        super.onStart();
    }

    @Override
    protected void onFinish() {
        // The phase is finished early when too few players are left, the game ends then and no roles are needed
        if (getCurrentTicks() > getEndTicks()) return;
        // The roles are allocated at the end, otherwise the items and the names in the tab list reveal them too early
        TeamHelper.allocateTeams(this.teamService);
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
        // The first update runs directly at the start and the phase finishes one update after the last tick
        Audience players = Audience.audience(MinecraftServer.getConnectionManager().getOnlinePlayers());
        players.sendMessage(GameMessages.getRoundStartTime(getCurrentTicks() + 1));
        players.playSound(PLING, Sound.Emitter.self());
    }
}
