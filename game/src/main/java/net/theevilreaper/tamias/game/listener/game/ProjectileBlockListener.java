package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.theevilreaper.tamias.game.util.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/

public final class ProjectileBlockListener implements Consumer<ProjectileCollideWithBlockEvent> {

    @Override
    public void accept(@NotNull ProjectileCollideWithBlockEvent event) {
        if (event.getEntity().hasTag(Tags.SHOOTER_ID)) return;
        event.getEntity().remove();
    }
}
