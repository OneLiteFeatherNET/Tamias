package net.theevilreaper.tamias.game.round;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.player.TamiasPlayer;
import net.theevilreaper.tamias.game.util.GameMessages;

import java.util.Collection;

/**
 * The {@link GameResultAnnouncer} shows each player its own stats at the end of the game.
 * There is no overall winner, so nothing is broadcast.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public final class GameResultAnnouncer {

    /**
     * Sends every given player one message with its round wins, kills and hits.
     * Nothing is sent when no round was played.
     *
     * @param playedRounds the amount of rounds which were played
     * @param players      the players which receive their result
     */
    public static void announce(int playedRounds, Collection<? extends Player> players) {
        if (playedRounds == 0) return;

        for (Player player : players) {
            if (!(player instanceof TamiasPlayer tamiasPlayer)) continue;
            player.sendMessage(GameMessages.getPersonalResult(
                    tamiasPlayer.getRoundWins(),
                    playedRounds,
                    tamiasPlayer.getKills(),
                    tamiasPlayer.getHits()
            ));
        }
    }

    private GameResultAnnouncer() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
