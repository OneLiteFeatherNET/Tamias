package net.theevilreaper.tamias.game.team.component;

import net.minestom.server.entity.EntityType;
import net.theevilreaper.xerus.api.component.ObjectComponent;

/**
 * Component attached to a Team defining its entity representation.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
public record EntityComponent(EntityType entityType) implements ObjectComponent {
}
