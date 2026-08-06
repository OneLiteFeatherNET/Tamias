package net.theevilreaper.tamias.game.team.component;

import net.minestom.server.entity.Player;
import net.theevilreaper.tamias.game.stamina.StaminaBar;
import net.theevilreaper.xerus.api.component.ObjectComponent;

import java.util.function.Function;

/**
 * Component attached to a Team defining how its StaminaBar is created for players.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record StaminaComponent(Function<Player, StaminaBar> staminaFactory) implements ObjectComponent {
}
