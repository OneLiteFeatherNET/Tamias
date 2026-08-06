package net.theevilreaper.tamias.game.team.component;

import net.minestom.server.entity.Player;
import net.theevilreaper.xerus.api.component.ObjectComponent;

import java.util.function.Consumer;

/**
 * Component attached to a Team defining item assignment for its players.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record ItemComponent(Consumer<Player> itemApplier) implements ObjectComponent {
}
