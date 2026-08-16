package net.theevilreaper.tamias.game.listener.game;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.theevilreaper.tamias.common.config.GameConfig;
import net.theevilreaper.tamias.common.util.Tags;
import net.theevilreaper.tamias.game.event.bomber.BomberEliminatedEvent;

import java.util.function.Consumer;

/**
 * Removes the projectile on collision and, if the target is a tagged Bomber,
 * dispatches {@link BomberEliminatedEvent}. Hits on Survivors are ignored —
 * only self-detonation (see {@link BomberExplodeListener}) can convert a Survivor to Bomber.
 *
 * @author theEvilReaper
 * @version 2.0.0
 * @since 1.0.0
 **/
public final class ProjectileEntityListener implements Consumer<ProjectileCollideWithEntityEvent> {

    @Override
    public void accept(ProjectileCollideWithEntityEvent event) {
        event.getEntity().remove();
        var target = event.getTarget();

        if (!(target instanceof Player targetPlayer)) return;
        if (!target.hasTag(Tags.TEAM_KEY)) return;

        String teamKey = target.getTag(Tags.TEAM_KEY);
        if (!GameConfig.BOMBER_KEY.asString().equals(teamKey)) return;

        EventDispatcher.call(new BomberEliminatedEvent(targetPlayer, targetPlayer.getPosition().asVec()));
    }
}
