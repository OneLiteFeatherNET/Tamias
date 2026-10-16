package net.theevilreaper.tamias.game.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

/**
 * The interface represents the basic structure of a scoreboard for the game.
 * It is used to display different information to the player.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public interface Scoreboard {

    /**
     * Initializes the default values for the scoreboard.
     */
    void initDefaults();

    /**
     * Updates the title of the scoreboard.
     *
     * @param title the new title to set
     */
    void updateTitle(Component title);

    /**
     * Adds the player to the scoreboard viewers.
     *
     * @param player the player to add
     */
    void addViewer(Player player);

    /**
     * Removes the player from the scoreboard viewers.
     *
     * @param player the player to remove
     */
    void removeViewer(Player player);

    /**
     * Updates a given score in the scoreboard with a new component.
     *
     * @param scoreType the type of score to update
     * @param component the new component to set for the score
     */
    void updateScore(ScoreType scoreType, Component component);
}
